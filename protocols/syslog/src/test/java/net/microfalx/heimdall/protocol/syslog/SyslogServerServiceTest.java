package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.AbstractSyslogMessageSender;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.cloudbees.syslog.sender.TcpSyslogMessageSender;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ThreadUtils.sleepSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SyslogServerServiceTest {

    private static final AtomicInteger IDENTIFIER = new AtomicInteger(1);

    @Mock
    private SyslogService syslogService;

    @Spy
    private SyslogConfiguration configuration = new SyslogConfiguration();

    @InjectMocks
    private SyslogServerService serverService;

    @BeforeEach
    void setup() {
        configuration.setTcpPort(2601);
        configuration.setUdpPort(2514);
        serverService.initialize();
    }

    @AfterEach
    void destroy() {
        serverService.destroy();
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
        sleepSeconds(1);
        assertEvents();
    }

    @Test
    void sendUdp() throws IOException {
        SyslogMessageSender client = createSyslogSender("udp");
        sendLogs(client);
        sleepSeconds(2);
        assertEvents();
    }

    private void assertEvents() {
        ArgumentCaptor<net.microfalx.heimdall.protocol.syslog.SyslogMessage> syslogCapture = ArgumentCaptor.forClass(net.microfalx.heimdall.protocol.syslog.SyslogMessage.class);
        // TODO change to hande method, to capture the message
        Mockito.verify(syslogService, Mockito.times(5)).index(syslogCapture.capture());
        Iterator<net.microfalx.heimdall.protocol.syslog.SyslogMessage> iterator = syslogCapture.getAllValues().iterator();
        net.microfalx.heimdall.protocol.syslog.SyslogMessage message = iterator.next();
        assertEquals("", message.getName());
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
        //syslogMessage.setMsgId(Integer.toString(IDENTIFIER.getAndIncrement()));
        syslogMessage.withMsg(message);
        syslogMessage.setTimestamp(new Date());
        return syslogMessage;
    }

    private SyslogMessageSender createSyslogSender(String protocol) {
        AbstractSyslogMessageSender messageSender;
        if ("udp".equalsIgnoreCase(protocol)) {
            UdpSyslogMessageSender udpMessageSender = new UdpSyslogMessageSender();
            udpMessageSender.setSyslogServerPort(configuration.getUdpPort());
            messageSender = udpMessageSender;
        } else {
            TcpSyslogMessageSender tcpMessageSender = new TcpSyslogMessageSender();
            tcpMessageSender.setSyslogServerPort(configuration.getTcpPort());
            messageSender = tcpMessageSender;
        }
        messageSender.setSyslogServerHostname("localhost");
        messageSender.setDefaultMessageHostname("localhost");
        messageSender.setMessageFormat(MessageFormat.RFC_5424);
        return messageSender;
    }

}