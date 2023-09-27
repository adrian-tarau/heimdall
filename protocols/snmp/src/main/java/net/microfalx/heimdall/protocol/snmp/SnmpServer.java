package net.microfalx.heimdall.protocol.snmp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolException;
import net.microfalx.heimdall.protocol.snmp.mib.MibModule;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;
import net.microfalx.lang.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.WorkerPool;
import org.snmp4j.util.WorkerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.append;
import static org.apache.commons.lang3.StringUtils.abbreviate;

@Component
public class SnmpServer implements CommandResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpServer.class);

    @Autowired
    private SnmpProperties configuration;

    @Autowired
    private SnmpService snmpService;

    private MessageDispatcher messageDispatcher;
    private Snmp snmpV2server;
    private Snmp snmpV3server;

    @Autowired
    private MibService mibService;

    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> event) {
        event.setProcessed(true);
        PDU pdu = event.getPDU();
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Received trap from {}, PDU: {}", event.getPeerAddress(), pdu);
        SnmpEvent snmpEvent = new SnmpEvent();
        for (VariableBinding variable : pdu.getVariableBindings()) {
            updateBindings(snmpEvent, variable);
        }
        updateCommonAttributes(snmpEvent, pdu);
        updateAddresses(snmpEvent, event);
        snmpService.accept(snmpEvent);
    }

    @PostConstruct
    protected void initialize() {
        initializeV2Server();
        initializeV3Server();
    }

    @PreDestroy
    protected void destroy() {
        if (snmpV2server != null) IOUtils.closeQuietly(snmpV2server);
        if (snmpV3server != null) IOUtils.closeQuietly(snmpV3server);
    }

    private void initializeV2Server() {
        initializeDispatcher();
        try {
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/" + configuration.getUdpPort()));
            snmpV2server = new Snmp(messageDispatcher);
            snmpV2server.addTransportMapping(transport);
            snmpV2server.addCommandResponder(this);
            snmpV2server.listen();
        } catch (IOException e) {
            throw new ProtocolException("Failed to start SNMP server on " + configuration.getUdpPort(), e);
        }
    }

    private void initializeV3Server() {
        initializeDispatcher();
        try {
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/" + (configuration.getUdpPort() + 1)));
            snmpV3server = new Snmp(messageDispatcher);
            byte[] localEngineID = MPv3.createLocalEngineID();
            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(localEngineID), 0);
            SecurityModels.getInstance().addSecurityModel(usm);
            snmpV3server.setLocalEngine(localEngineID, 0, 0);
            // TODO Add the configured user to the USM
            snmpV3server.addTransportMapping(transport);
            snmpV3server.addCommandResponder(this);
            snmpV3server.listen();
        } catch (IOException e) {
            throw new ProtocolException("Failed to start SNMP server on " + configuration.getUdpPort(), e);
        }
    }

    private void initializeDispatcher() {
        if (messageDispatcher != null) return;
        MessageDispatcher dispatcher = new MessageDispatcherImpl();
        dispatcher.addCommandResponder(this);
        dispatcher.addMessageProcessingModel(new MPv1());
        dispatcher.addMessageProcessingModel(new MPv2c());
        dispatcher.addMessageProcessingModel(new MPv3());
        SecurityProtocols.getInstance().addDefaultProtocols();
        messageDispatcher = new MultiThreadedMessageDispatcher(new WorkerPoolImpl(snmpService.getTaskExecutor()), dispatcher);
    }

    private void updateCommonAttributes(SnmpEvent event, PDU pdu) {
        event.setCommunity("public");
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
        if (sentAt != null) {
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
        String attributeNameFromOid = mibService.findName(attributeName, false);
        if (attributeNameFromOid != null) attributeName = attributeNameFromOid;
        Variable attributeValue = variable.getVariable();
        if (attributeValue instanceof Integer32) {
            snmpEvent.addAttribute(attributeName, ((Integer32) attributeValue).getValue());
        } else if (attributeValue instanceof TimeTicks) {
            snmpEvent.addAttribute(attributeName, attributeValue.toLong());
        } else if (attributeValue instanceof Null) {
            snmpEvent.addAttribute(attributeName, null);
        } else if (attributeValue instanceof OctetString) {
            snmpEvent.addAttribute(attributeName, attributeValue.toString());
        } else if (attributeValue instanceof Counter64) {
            snmpEvent.addAttribute(attributeName, attributeValue.toLong());
        } else if (attributeValue instanceof UnsignedInteger32) {
            snmpEvent.addAttribute(attributeName, attributeValue.toInt());
        } else if (attributeValue instanceof OID) {
            snmpEvent.addAttribute(attributeName, ((OID) attributeValue).toDottedString());
        } else if (attributeValue instanceof IpAddress) {
            snmpEvent.addAttribute(attributeName, ((IpAddress) attributeValue).getInetAddress().getHostAddress());
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

    static class WorkerPoolImpl implements WorkerPool {

        private final TaskExecutor executor;

        WorkerPoolImpl(TaskExecutor executor) {
            requireNonNull(executor);
            this.executor = executor;
        }

        @Override
        public void execute(WorkerTask task) {
            executor.execute(task);
        }

        @Override
        public boolean tryToExecute(WorkerTask task) {
            try {
                executor.execute(task);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void stop() {
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isIdle() {
            return false;
        }
    }

    private static final Set<String> commonOidPrefixes = new HashSet<>();

    static {
        commonOidPrefixes.add("1.3.6.1.2.1.1");
        commonOidPrefixes.add("1.3.6.1.6.3.1.1.4");
    }
}
