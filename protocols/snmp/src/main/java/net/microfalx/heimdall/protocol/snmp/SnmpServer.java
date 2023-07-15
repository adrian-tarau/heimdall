package net.microfalx.heimdall.protocol.snmp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.microfalx.heimdall.protocol.core.ProtocolException;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.ZonedDateTime;

@Component
public class SnmpServer implements CommandResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnmpServer.class);

    @Autowired
    private SnmpProperties configuration;

    @Autowired
    private SnmpService snmpService;

    private ThreadPoolTaskExecutor executor;
    private MessageDispatcher messageDispatcher;
    private Snmp snmpV2server;
    private Snmp snmpV3server;

    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> event) {
        event.setProcessed(true);
        PDU pdu = event.getPDU();
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Received trap from {}, PDU: {}", event.getPeerAddress(), pdu);
        SnmpEvent snmpEvent = new SnmpEvent();
        updateCommonAttributes(snmpEvent, pdu);
        updateAddresses(snmpEvent, event);
        for (VariableBinding variable : pdu.getVariableBindings()) {
            updateBindings(snmpEvent, variable);
        }
        snmpService.accept(snmpEvent);
    }

    @PostConstruct
    protected void initialize() {
        initThreadPool();
        initializeV2Server();
        initializeV3Server();
    }

    @PreDestroy
    protected void destroy() {
        if (snmpV2server != null) IOUtils.closeQuietly(snmpV2server);
        if (snmpV3server != null) IOUtils.closeQuietly(snmpV3server);
        if (executor != null) executor.destroy();
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
        messageDispatcher = new MultiThreadedMessageDispatcher(new WorkerPoolImpl(executor), dispatcher);
    }

    private void initThreadPool() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("heimdall-snmp");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
    }

    private void updateCommonAttributes(SnmpEvent event, PDU pdu) {
        event.setEnterprise("dummy");
        event.setCommunity("public");
        event.setCreatedAt(ZonedDateTime.now());
        event.setSentAt(ZonedDateTime.now());
        event.setReceivedAt(ZonedDateTime.now());
        event.setName("SNMP Trap");
    }

    private <A extends Address> void updateAddresses(SnmpEvent snmpEvent, CommandResponderEvent<A> event) {
        InetSocketAddress address = (InetSocketAddress) event.getPeerAddress().getSocketAddress();
        snmpEvent.setSource(net.microfalx.heimdall.protocol.core.Address.
                create(net.microfalx.heimdall.protocol.core.Address.Type.HOSTNAME,
                        address.getAddress().getHostAddress()));
    }

    private void updateBindings(SnmpEvent snmpEvent, VariableBinding variable) {
        String attributeName = variable.getOid().format();
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

    static class WorkerPoolImpl implements WorkerPool {

        private final ThreadPoolTaskExecutor executor;

        WorkerPoolImpl(ThreadPoolTaskExecutor executor) {
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
            executor.afterPropertiesSet();
        }

        @Override
        public boolean isIdle() {
            return false;
        }
    }
}
