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
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.WorkerPool;
import org.snmp4j.util.WorkerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
        // TODO translate traps to our trap event, the PDU contains the info in bindings, and the event the rest of the ino
        LOGGER.info("Received trap from " + event.getPeerAddress() + ", PDU: " + pdu);
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
