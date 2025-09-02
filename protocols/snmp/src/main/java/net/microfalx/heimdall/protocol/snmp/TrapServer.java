package net.microfalx.heimdall.protocol.snmp;

import jakarta.annotation.PreDestroy;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.snmp.mib.MibModule;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.PDU;
import org.snmp4j.smi.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static net.microfalx.heimdall.protocol.core.ProtocolConstants.MAX_NAME_LENGTH;
import static net.microfalx.heimdall.protocol.snmp.SnmpUtils.describeAddress;
import static net.microfalx.lang.StringUtils.append;
import static org.apache.commons.lang3.StringUtils.abbreviate;

@Component
public class TrapServer implements CommandResponder, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrapServer.class);

    private static final Metrics TRAP_METRICS = SnmpUtils.METRICS.withGroup(EnumUtils.toLabel(SnmpMode.TRAP)).withGroup("Trap");
    private static final Metrics TRAP_FAILURE_METRICS = TRAP_METRICS.withGroup(EnumUtils.toLabel(SnmpMode.TRAP)).withGroup("Failure");

    @Autowired private SnmpService snmpService;
    @Autowired(required = false) private SnmpProperties properties;
    @Autowired private MibService mibService;

    private MessageDispatcher messageDispatcher;

    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> event) {
        if (event.getPDU().getType() != PDU.TRAP) return;
        event.setProcessed(true);
        PDU pdu = event.getPDU();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received trap from {}, PDU: {}", describeAddress(event.getPeerAddress()), pdu);
        }
        try {
            SnmpEvent snmpEvent = new SnmpEvent();
            for (VariableBinding variable : pdu.getVariableBindings()) {
                updateBindings(snmpEvent, variable);
            }
            updateCommonAttributes(snmpEvent, pdu);
            updateAddresses(snmpEvent, event);
            snmpService.accept(snmpEvent);
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Failed to process SNMP trap from {}, PDU size: {}", describeAddress(event.getPeerAddress()), pdu.getBERLength());
            TRAP_FAILURE_METRICS.count(ExceptionUtils.getRootCauseName(e));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        messageDispatcher = snmpService.createDispatcher(SnmpMode.TRAP);
        messageDispatcher.addCommandResponder(this);
    }

    @PreDestroy
    public void destroy() {
        messageDispatcher.stop();
    }

    private void updateCommonAttributes(SnmpEvent event, PDU pdu) {
        event.setCommunity(properties.getTrapComunityString());
        Collection<VariableBinding> variableBindings = getFilteredBindings(pdu);
        MibModule module = null;
        for (VariableBinding variableBinding : variableBindings) {
            String oid = variableBinding.getOid().toDottedString();
            MibVariable variable = mibService.findVariable(oid);
            if (variable != null && module == null) {
                module = variable.getModule();
                event.setEnterprise(module.getEnterpriseOid());
            }
        }
        VariableBinding sentAt = null;
        VariableBinding severity = null;
        VariableBinding message = null;
        if (module != null) {
            for (VariableBinding variableBinding : variableBindings) {
                String oid = variableBinding.getOid().toDottedString();
                if (message == null && module.getMessageOids().contains(oid)) message = variableBinding;
                if (sentAt == null && module.getSentAtOids().contains(oid)) sentAt = variableBinding;
                if (severity == null && module.getSeverityOids().contains(oid)) severity = variableBinding;
            }
        }
        updateSentAt(event, sentAt);
        updateSeverity(event, severity);
        if (message != null) {
            String messageAsString = message.toValueString();
            event.setName(messageAsString);
            event.setBody(Body.create(messageAsString));
        } else {
            updateNameFromAttributes(event);
        }
    }

    private void updateSeverity(SnmpEvent event, VariableBinding severity) {
        if (severity != null) event.setSeverity(severity.toValueString());
    }

    private void updateSentAt(SnmpEvent event, VariableBinding sentAt) {
        if (sentAt != null && SnmpUtils.isInteger(sentAt.getVariable())) {
            long tick = sentAt.getVariable().toLong();
            ZonedDateTime sentAtDt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tick * 1000), ZoneId.systemDefault());
            event.setCreatedAt(sentAtDt);
            event.setSentAt(sentAtDt);
        } else {
            event.setCreatedAt(ZonedDateTime.now());
            event.setSentAt(ZonedDateTime.now());
        }
        event.setReceivedAt(ZonedDateTime.now());
    }

    private void updateNameFromAttributes(SnmpEvent event) {
        Iterator<Attribute> entries = event.iterator();
        StringBuilder nameBuilder = new StringBuilder();
        StringBuilder bodyBuilder = new StringBuilder();
        int count = 3;
        while (entries.hasNext()) {
            Attribute attribute = entries.next();
            if (count-- > 0) {
                String nameFragment = abbreviate(attribute.asString(), MAX_NAME_LENGTH / 3);
                append(nameBuilder, attribute.getLabel() + ": " + nameFragment, " / ");
            }
            append(bodyBuilder, attribute.getLabel() + ": " + attribute.asString(), "\n");
        }
        event.setName(nameBuilder.toString());
        event.setBody(Body.create(event.getName()));
    }

    private <A extends Address> void updateAddresses(SnmpEvent snmpEvent, CommandResponderEvent<A> event) {
        InetSocketAddress address = (InetSocketAddress) event.getPeerAddress().getSocketAddress();
        snmpEvent.setSource(net.microfalx.heimdall.protocol.core.Address.
                create(net.microfalx.heimdall.protocol.core.Address.Type.HOSTNAME,
                        address.getAddress().getHostAddress()));
    }

    private void updateBindings(SnmpEvent snmpEvent, VariableBinding variable) {
        String attributeName = variable.getOid().toDottedString();
        String attributeNameFromOid = mibService.findName(attributeName, false, false);
        if (attributeNameFromOid != null) attributeName = attributeNameFromOid;
        Variable attributeValue = variable.getVariable();
        if (attributeValue instanceof Integer32) {
            snmpEvent.add(attributeName, ((Integer32) attributeValue).getValue());
        } else if (attributeValue instanceof TimeTicks) {
            snmpEvent.add(attributeName, attributeValue.toLong());
        } else if (attributeValue instanceof Null) {
            snmpEvent.add(attributeName, null);
        } else if (attributeValue instanceof OctetString) {
            snmpEvent.add(attributeName, attributeValue.toString());
        } else if (attributeValue instanceof Counter64) {
            snmpEvent.add(attributeName, attributeValue.toLong());
        } else if (attributeValue instanceof UnsignedInteger32) {
            snmpEvent.add(attributeName, attributeValue.toInt());
        } else if (attributeValue instanceof OID) {
            snmpEvent.add(attributeName, mibService.findName(((OID) attributeValue).toDottedString(), false, true));
        } else if (attributeValue instanceof IpAddress) {
            snmpEvent.add(attributeName, ((IpAddress) attributeValue).getInetAddress().getHostAddress());
        }
    }

    private Collection<VariableBinding> getFilteredBindings(PDU pdu) {
        return pdu.getVariableBindings().stream().filter(v -> !isCommonOid(v.getOid())).collect(Collectors.toList());
    }

    private boolean isCommonOid(OID oid) {
        String oldString = oid.toDottedString();
        for (String commonOidPrefix : commonOidPrefixes) {
            if (oldString.startsWith(commonOidPrefix)) return true;
        }
        return false;
    }

    private static final Set<String> commonOidPrefixes = new HashSet<>();

    static {
        SnmpLogger.init();
        commonOidPrefixes.add("1.3.6.1.2.1.1");
        commonOidPrefixes.add("1.3.6.1.6.3.1.1.4");
    }
}
