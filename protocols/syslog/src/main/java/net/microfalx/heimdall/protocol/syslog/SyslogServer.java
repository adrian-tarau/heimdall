package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import jakarta.annotation.PreDestroy;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import org.graylog2.syslog4j.impl.message.structured.StructuredSyslogMessage;
import org.graylog2.syslog4j.server.SyslogServerEventIF;
import org.graylog2.syslog4j.server.SyslogServerIF;
import org.graylog2.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.graylog2.syslog4j.server.impl.event.structured.StructuredSyslogServerEvent;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServer;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServer;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static net.microfalx.heimdall.protocol.core.ProtocolConstants.MAX_NAME_LENGTH;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.StringUtils.removeLineBreaks;
import static org.apache.commons.lang3.StringUtils.abbreviate;

/**
 * Service for all Syslog servers, TPC and UDP protocols.
 */
@Component
public class SyslogServer implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyslogServer.class);

    @Autowired
    private SyslogConfiguration configuration;

    @Autowired
    private SyslogService syslogService;

    private SyslogListener listener = new SyslogListener();
    private TCPNetSyslogServer tcpServer;
    private UDPNetSyslogServer udpServer;

    public SyslogConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    public void initialize() {
        initializeTcpServer();
        initializeUdpServer();
    }

    @PreDestroy
    protected void destroy() {
        if (tcpServer != null) tcpServer.shutdown();
        if (udpServer != null) udpServer.shutdown();
    }

    private void initializeTcpServer() {
        TCPNetSyslogServerConfig config = new TCPNetSyslogServerConfig(configuration.getTcpPort());
        config.addEventHandler(listener);
        config.setUseDaemonThread(true);
        config.setUseStructuredData(true);
        tcpServer = new TCPNetSyslogServer();
        tcpServer.initialize("TCP", config);
        syslogService.getTaskExecutor().execute(tcpServer);
    }

    private void initializeUdpServer() {
        UDPNetSyslogServerConfig config = new UDPNetSyslogServerConfig(configuration.getUdpPort());
        config.addEventHandler(listener);
        config.setUseDaemonThread(true);
        config.setUseStructuredData(true);
        udpServer = new UDPNetSyslogServer();
        udpServer.initialize("UDP", config);
        syslogService.getTaskExecutor().execute(udpServer);
    }

    private void event(SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event) {
        InetSocketAddress address = (InetSocketAddress) socketAddress;
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Received syslog even from {}", address.getHostName());
        StructuredSyslogServerEvent structuredEvent = null;
        if (event instanceof StructuredSyslogServerEvent) {
            structuredEvent = (StructuredSyslogServerEvent) event;
        }
        String originalMessage = event.getMessage();
        StructuredSyslogMessage structuredMessage = structuredEvent.getStructuredMessage();
        if (structuredMessage != null) {
            originalMessage = structuredMessage.getMessage();
        }
        SyslogMessage message = new SyslogMessage();
        message.setName(abbreviate(removeLineBreaks(originalMessage), MAX_NAME_LENGTH));
        message.setFacility(Facility.fromNumericalCode(event.getFacility()));
        message.setSyslogSeverity(Severity.fromNumericalCode(event.getLevel()));
        message.setSource(Address.create(Address.Type.HOSTNAME, address.getHostName(), address.getAddress().getHostAddress()));
        message.addTarget(Address.create(Address.Type.HOSTNAME, event.getHost()));
        message.setBody(Body.create(originalMessage));
        message.setCreatedAt(event.getDate().toInstant().atZone(ZoneId.systemDefault()));
        message.setSentAt(message.getCreatedAt());
        message.setReceivedAt(ZonedDateTime.now());
        message.addAttribute("server", event.getHost());
        if (structuredEvent != null) {
            message.addAttribute("processId", structuredEvent.getProcessId());
            message.addAttribute("application", structuredEvent.getApplicationName());
            if (isNotEmpty(structuredMessage.getMessageId()))
                message.addAttribute("messageId", structuredMessage.getMessageId());
            if (isNotEmpty(structuredMessage.getProcId())) message.addAttribute("pid", structuredMessage.getProcId());
        }
        syslogService.accept(message);
    }

    private class SyslogListener implements SyslogServerSessionEventHandlerIF {

        @Override
        public Object sessionOpened(SyslogServerIF syslogServer, SocketAddress socketAddress) {
            return new SyslogSession(socketAddress);
        }

        @Override
        public void event(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event) {
            SyslogServer.this.event(syslogServer, socketAddress, event);
        }

        @Override
        public void exception(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, Exception exception) {

        }

        @Override
        public void sessionClosed(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, boolean timeout) {

        }

        @Override
        public void initialize(SyslogServerIF syslogServer) {

        }

        @Override
        public void destroy(SyslogServerIF syslogServer) {

        }
    }
}
