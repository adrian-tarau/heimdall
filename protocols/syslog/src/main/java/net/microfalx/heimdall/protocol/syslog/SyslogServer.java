package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import jakarta.annotation.PreDestroy;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import org.graylog2.syslog4j.server.SyslogServerEventIF;
import org.graylog2.syslog4j.server.SyslogServerIF;
import org.graylog2.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServer;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServer;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Service for all Syslog servers, TPC and UDP protocols.
 */
@Component
public class SyslogServer implements InitializingBean {

    @Autowired
    private SyslogConfiguration configuration;

    @Autowired
    private SyslogService syslogService;

    private SyslogListener listener = new SyslogListener();
    private ThreadPoolTaskExecutor executor;
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
        initThreadPool();
        initializeTcpServer();
        initializeUdpServer();
    }

    @PreDestroy
    protected void destroy() {
        if (tcpServer != null) tcpServer.shutdown();
        if (udpServer != null) udpServer.shutdown();
        if (executor != null) executor.destroy();
    }

    private void initializeTcpServer() {
        TCPNetSyslogServerConfig config = new TCPNetSyslogServerConfig(configuration.getTcpPort());
        config.addEventHandler(listener);
        config.setUseDaemonThread(true);
        config.setUseStructuredData(true);
        tcpServer = new TCPNetSyslogServer();
        tcpServer.initialize("TCP", config);
        executor.execute(tcpServer);
    }

    private void initializeUdpServer() {
        UDPNetSyslogServerConfig config = new UDPNetSyslogServerConfig(configuration.getUdpPort());
        config.addEventHandler(listener);
        config.setUseDaemonThread(true);
        config.setUseStructuredData(true);
        udpServer = new UDPNetSyslogServer();
        udpServer.initialize("UDP", config);
        executor.execute(udpServer);
    }

    private void event(SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event) {
        SyslogMessage message = new SyslogMessage();
        message.setFacility(Facility.fromNumericalCode(event.getFacility()));
        message.setSyslogSeverity(Severity.fromNumericalCode(event.getLevel()));
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        message.setSource(Address.create(Address.Type.HOSTNAME, inetSocketAddress.getAddress().getHostName(),
                inetSocketAddress.getAddress().getHostAddress()));
        message.addTarget(Address.create(Address.Type.HOSTNAME, event.getHost()));
        message.setName("Syslog");
        message.setBody(Body.create(message.getName()));
        message.setCreatedAt(event.getDate().toInstant().atZone(ZoneId.systemDefault()));
        message.setSentAt(message.getCreatedAt());
        message.setReceivedAt(ZonedDateTime.now());
        syslogService.handle(message);
    }

    private void initThreadPool() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("heimdall-syslog");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
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
