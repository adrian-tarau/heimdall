package net.microfalx.heimdall.protocol.snmp;

import jakarta.annotation.PreDestroy;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEventRepository;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.IOUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.event.AuthenticationFailureEvent;
import org.snmp4j.event.AuthenticationFailureListener;
import org.snmp4j.event.CounterEvent;
import org.snmp4j.event.CounterListener;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.nonstandard.PrivAES256With3DESKeyExtension;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportListener;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.WorkerPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.microfalx.heimdall.protocol.snmp.SnmpUtils.describeAddress;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;

@Service
public final class SnmpService extends ProtocolService<SnmpEvent, net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpService.class);

    private static final Metrics DISPATCHER_METRICS = SnmpUtils.METRICS.withGroup("Dispatcher");

    @Autowired private MibService mibService;
    @Autowired private SnmpSimulator simulator;
    @Autowired private SnmpProperties properties;
    @Autowired private SnmpEventRepository repository;

    private WorkerPool workerPool;
    private final Collection<TransportMapping<?>> transportMappings = new CopyOnWriteArrayList<>();

    public MibService getMibService() {
        return mibService;
    }

    @Override
    protected boolean isSimulatorEnabled(boolean enabled) {
        if (super.isSimulatorEnabled(enabled)) {
            return true;
        } else {
            return properties.isSimulatorEnabled();
        }
    }

    @Override
    protected SnmpSimulator getSimulator() {
        return simulator;
    }

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SNMP;
    }

    @Override
    protected String getControllerPath() {
        return "/protocol/snmp";
    }

    @Override
    protected void prepare(SnmpEvent event) {
        lookupAddress(event.getSource());
    }

    protected void persist(SnmpEvent event) {
        net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent snmpEvent = new net.
                microfalx.heimdall.protocol.snmp.jpa.SnmpEvent();
        snmpEvent.setAgentAddress(lookupAddress(event.getSource()));
        snmpEvent.setCreatedAt(event.getCreatedAt().toLocalDateTime());
        snmpEvent.setSentAt(event.getSentAt().toLocalDateTime());
        snmpEvent.setReceivedAt(event.getReceivedAt().toLocalDateTime());
        snmpEvent.setVersion(event.getVersion());

        snmpEvent.setCommunityString(event.getCommunity());
        snmpEvent.setEnterprise(event.getEnterprise());
        snmpEvent.setTrapType(PDU.TRAP);

        snmpEvent.setMessage(persistPart(event.getBody()));
        event.setBody(Body.create(event.toJson()));
        snmpEvent.setBindingPart(persistPart(event.getBody()));

        repository.save(snmpEvent);
        updateReference(event, snmpEvent.getId());
    }

    @Override
    protected Resource getAttributesResource(net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent model) {
        return ResourceFactory.resolve(model.getBindingPart().getResource()).withMimeType(MimeType.APPLICATION_JSON);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        initWorkerPool();
        initSecurity();
        simulator.setSnmpService(this);
    }

    @PreDestroy
    public void destroy() {
        destroyTransport();
    }

    private void initWorkerPool() {
        workerPool = new SnmpWorkerPool(getThreadPool());
    }

    WorkerPool getWorkerPool() {
        return workerPool;
    }

    MessageDispatcher createDispatcher(SnmpMode mode) {
        MessageDispatcherImpl dispatcher = new MessageDispatcherImpl();
        dispatcher.addMessageProcessingModel(new MPv1());
        dispatcher.addMessageProcessingModel(new MPv2c());
        dispatcher.addMessageProcessingModel(new MPv3());

        dispatcher.addCommandResponder(new CommandResponderImpl(mode));
        dispatcher.addAuthenticationFailureListener(new AuthenticationFailureListenerImpl());

        initTransport(dispatcher, mode);
        dispatcher.addCounterListener(new CounterListenerImpl());
        return new MultiThreadedMessageDispatcher(getWorkerPool(), dispatcher);
    }

    private void initSecurity() {
        SecurityProtocols.getInstance().addDefaultProtocols();
        // Uncomment the following if you want to use AES 192 or 256 with 3DES like key extension.
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES256With3DESKeyExtension());
    }

    private void initTransport(MessageDispatcher dispatcher, SnmpMode mode) {
        try {
            UdpAddress address = new UdpAddress("0.0.0.0/" + (mode == SnmpMode.AGENT ? properties.getAgentUdpPort() : properties.getTrapUdpPort()));
            TransportMapping<UdpAddress> udpTransport = new DefaultUdpTransportMapping(address);
            initTransport(dispatcher, udpTransport, mode);
        } catch (IOException e) {
            LOGGER.atError().setCause(e).log("Failed to create UDP transport");
        }
        try {
            TcpAddress address = new TcpAddress("0.0.0.0/" + (mode == SnmpMode.AGENT ? properties.getAgentTcpPort() : properties.getTrapTcpPort()));
            TransportMapping<TcpAddress> tcpTransport = new DefaultTcpTransportMapping(address);
            initTransport(dispatcher, tcpTransport, mode);
        } catch (IOException e) {
            LOGGER.atError().setCause(e).log("Failed to create TCP transport");
        }
    }

    private <A extends Address> void initTransport(MessageDispatcher dispatcher, TransportMapping<A> transportMapping, SnmpMode mode) throws IOException {
        transportMapping.addTransportListener(new TransportListenerImpl(mode));
        dispatcher.addTransportMapping(transportMapping);
        transportMapping.addTransportListener(dispatcher);
        transportMapping.listen();
        transportMappings.add(transportMapping);
    }

    private void destroyTransport() {
        for (TransportMapping<?> transportMapping : transportMappings) {
            IOUtils.closeQuietly(transportMapping);
        }
    }

    private String getOidName(OID oid) {
        String name = mibService.findName(oid.toDottedString(), false, true);
        return isNotEmpty(name) ? name : oid.toDottedString();
    }

    static {
        SnmpLogger.init();
    }

    class CounterListenerImpl implements CounterListener {

        private static final Metrics DISPATCHER_COUNTER_METRICS = DISPATCHER_METRICS.withGroup("Counter");

        @Override
        public void incrementCounter(CounterEvent event) {
            String oidName = getOidName(event.getOid());
            DISPATCHER_COUNTER_METRICS.count(oidName);
        }
    }

    private static class AuthenticationFailureListenerImpl implements AuthenticationFailureListener {

        private static final Metrics EVENT_METRICS = SnmpUtils.METRICS.withGroup("Authentication Failure");

        @Override
        public <A extends Address> void authenticationFailure(AuthenticationFailureEvent<A> event) {
            String errorType = unsupportedSecurityModelNames.getOrDefault(event.getError(), "Unknown Error");
            EVENT_METRICS.count(describeAddress(event.getAddress()) + " / " + errorType);
        }
    }

    private static class CommandResponderImpl implements CommandResponder {

        private final Metrics eventSecurityNameMetrics;
        private final Metrics eventSecurityModelMetrics;

        private final Metrics pduTypeMetrics;

        private CommandResponderImpl(SnmpMode mode) {
            requireNonNull(mode);

            Metrics eventMetrics = SnmpUtils.METRICS.withGroup(EnumUtils.toLabel(mode)).withGroup("Event");
            eventSecurityNameMetrics = eventMetrics.withGroup("Security Name");
            eventSecurityModelMetrics = eventMetrics.withGroup("Security Model");

            Metrics pduMetrics = SnmpUtils.METRICS.withGroup("PDU");
            pduTypeMetrics = pduMetrics.withGroup("Type");
        }

        @Override
        public <A extends Address> void processPdu(CommandResponderEvent<A> event) {
            OctetString securityName = new OctetString(event.getSecurityName());
            eventSecurityNameMetrics.count(securityName.toString());
            String securityModel = securityModelNames.getOrDefault(event.getSecurityModel(), "UNKNOWN");
            eventSecurityModelMetrics.count(securityModel);
            PDU pdu = event.getPDU();
            if (pdu == null) return;
            String pduType = PDU_TYPE_NAMES.getOrDefault(pdu.getType(), "UNKNOWN");
            this.pduTypeMetrics.count(pduType);
        }
    }

    private static class TransportListenerImpl implements TransportListener {

        private final Metrics transportMetrics;
        private final Metrics sourceAddressMetrics;
        private final Metrics incomingAddressMetrics;
        private final Metrics bytesMetrics;

        private TransportListenerImpl(SnmpMode mode) {
            requireNonNull(mode);

            transportMetrics = SnmpUtils.METRICS.withGroup(EnumUtils.toLabel(mode)).withGroup("Transport");
            sourceAddressMetrics = transportMetrics.withGroup("Source Address");
            incomingAddressMetrics = transportMetrics.withGroup("Incoming Address");
            bytesMetrics = transportMetrics.withGroup("Incoming Bytes");
        }

        @Override
        public <A extends Address> void processMessage(TransportMapping<? super A> sourceTransport, A incomingAddress,
                                                       ByteBuffer wholeMessage, TransportStateReference tmStateReference) {
            String listeningAddress = describeAddress(sourceTransport.getListenAddress());
            String incomingAddressString = describeAddress(incomingAddress);
            transportMetrics.count("Received");
            sourceAddressMetrics.count(listeningAddress);
            this.incomingAddressMetrics.count(incomingAddressString);
            bytesMetrics.count(incomingAddressString, wholeMessage.remaining());
        }
    }

    private static final Map<Integer, String> PDU_TYPE_NAMES = Map.of(
            PDU.GET, "GET",
            PDU.GETNEXT, "GET_NEXT",
            PDU.SET, "SET",
            PDU.RESPONSE, "RESPONSE",
            PDU.INFORM, "INFORM",
            PDU.TRAP, "TRAP",
            PDU.V1TRAP, "V1TRAP"
    );

    private static final Map<Integer, String> securityModelNames = Map.of(
            SecurityModel.SECURITY_MODEL_SNMPv1, "SNMPv1",
            SecurityModel.SECURITY_MODEL_SNMPv2c, "SNMPv2c",
            SecurityModel.SECURITY_MODEL_USM, "USM",
            SecurityModel.SECURITY_MODEL_TSM, "TSM"
    );

    private static final Map<Integer, String> unsupportedSecurityModelNames = new HashMap<>();

    static {
        unsupportedSecurityModelNames.putAll(Map.of(
                SnmpConstants.SNMP_MP_UNSUPPORTED_SECURITY_MODEL, "Unsupported Security Model",
                SnmpConstants.SNMPv3_USM_AUTHENTICATION_FAILURE, "Authentication Failure",
                SnmpConstants.SNMPv3_USM_UNSUPPORTED_SECURITY_LEVEL, "Unsupported Security Level",
                SnmpConstants.SNMPv3_USM_UNKNOWN_SECURITY_NAME, "Unknown Security Name",
                SnmpConstants.SNMPv3_USM_AUTHENTICATION_ERROR, "Authentication Error"));
        unsupportedSecurityModelNames.putAll(Map.of(
                SnmpConstants.SNMPv3_USM_NOT_IN_TIME_WINDOW, "Not in Time Window",
                SnmpConstants.SNMPv3_USM_UNSUPPORTED_AUTHPROTOCOL, "Unsupported Auth Protocol",
                SnmpConstants.SNMPv3_USM_UNKNOWN_ENGINEID, "Unknown Engine ID",
                SnmpConstants.SNMP_MP_WRONG_USER_NAME, "Wrong User Name",
                SnmpConstants.SNMPv3_TSM_INADEQUATE_SECURITY_LEVELS, "Inadequate Security Levels",
                SnmpConstants.SNMP_MP_USM_ERROR, "USM Error"));
    }
}
