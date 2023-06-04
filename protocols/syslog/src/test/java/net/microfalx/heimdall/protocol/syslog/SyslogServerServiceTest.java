package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SyslogServerServiceTest {

    private static final AtomicInteger IDENTIFIER = new AtomicInteger(1);

    @Mock
    private SyslogService smtpService;

    @Spy
    private SyslogConfiguration configuration = new SyslogConfiguration();

    @InjectMocks
    private SyslogServerService serverService;

    @BeforeEach
    void setup() {
        configuration.setTcpPort(2601);
        configuration.setUdpPort(2514);
    }

    @Test
    void initialize() {
        assertNotNull(serverService);
        serverService.initialize();
        SyslogConfiguration configuration = serverService.getConfiguration();
        assertEquals(2601, configuration.getTcpPort());
        assertEquals(2514, configuration.getUdpPort());
    }

    @Test
    void sendTcp() throws IOException {
        SyslogMessageSender client = createSyslogSender("tcp");
        sendLogs(client);
    }

    @Test
    void sendUdp() throws IOException {
        SyslogMessageSender client = createSyslogSender("udp");
        sendLogs(client);
    }

    private void sendLogs(SyslogMessageSender client) throws IOException {
        send(client, "Test 1", Severity.INFORMATIONAL);
        send(client, "Test 2", Severity.WARNING);
        send(client, "Test 3", Severity.ERROR);
        send(client, "Test 4", Severity.ALERT);
        send(client, "Test 5", Severity.CRITICAL);
    }

    private void send(SyslogMessageSender client, String message, Severity severity) throws IOException {
        com.cloudbees.syslog.SyslogMessage _message = createMessage(message, severity);
        client.sendMessage(_message);
    }

    private com.cloudbees.syslog.SyslogMessage createMessage(String message, Severity severity) {
        com.cloudbees.syslog.SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage.setAppName("Heimdall");
        syslogMessage.setFacility(Facility.LOCAL1);
        syslogMessage.setSeverity(severity);
        syslogMessage.setHostname("localhost");
        syslogMessage.setProcId("test");
        syslogMessage.setMsgId(Integer.toString(IDENTIFIER.getAndIncrement()));
        syslogMessage.withMsg(message);
        syslogMessage.setTimestamp(new Date());
        return syslogMessage;
    }

    private SyslogMessageSender createSyslogSender(String protocol) {
        UdpSyslogMessageSender messageSender = new UdpSyslogMessageSender();
        messageSender.setSyslogServerHostname("localhost");
        if ("udp".equalsIgnoreCase(protocol)) {
            messageSender.setSyslogServerPort(configuration.getUdpPort());
        } else {
            messageSender.setSyslogServerPort(configuration.getTcpPort());
        }
        messageSender.setDefaultMessageHostname("localhost");
        messageSender.setMessageFormat(MessageFormat.RFC_5424);
        return messageSender;
    }

}