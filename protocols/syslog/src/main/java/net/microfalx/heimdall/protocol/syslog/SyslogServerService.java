package net.microfalx.heimdall.protocol.syslog;

import jakarta.annotation.PostConstruct;
import org.graylog2.syslog4j.server.SyslogServerEventIF;
import org.graylog2.syslog4j.server.SyslogServerIF;
import org.graylog2.syslog4j.server.SyslogServerSessionEventHandlerIF;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServer;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServer;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.net.SocketAddress;

/**
 * Service for all Syslog servers, TPC and UDP protocols.
 */
@Service
public class SyslogServerService {

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

    @PostConstruct
    protected void initialize() {
        initThreadPool();
        initializeTcpServer();
        initializeUdpServer();
    }

    private void initializeTcpServer() {
        TCPNetSyslogServerConfig config = new TCPNetSyslogServerConfig(configuration.getTcpPort());
        config.addEventHandler(listener);
        config.setUseDaemonThread(true);
        tcpServer = new TCPNetSyslogServer();
        tcpServer.initialize("TCP", config);
        executor.execute(tcpServer);
    }

    private void initializeUdpServer() {
        UDPNetSyslogServerConfig config = new UDPNetSyslogServerConfig(configuration.getUdpPort());
        config.addEventHandler(listener);
        config.setUseDaemonThread(true);
        udpServer = new UDPNetSyslogServer();
        udpServer.initialize("UDP", config);
        executor.execute(udpServer);
    }

    private void event(SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event) {
        // TODO handle the syslog even here
    }

    private void initThreadPool() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("heimdall-syslog");
        executor.initialize();
    }

    private class SyslogListener implements SyslogServerSessionEventHandlerIF {

        @Override
        public Object sessionOpened(SyslogServerIF syslogServer, SocketAddress socketAddress) {
            return null;
        }

        @Override
        public void event(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event) {
            SyslogServerService.this.event(syslogServer, socketAddress, event);
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
