package net.microfalx.heimdall.protocol.syslog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Iterator;

import static net.microfalx.lang.ThreadUtils.sleepSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SyslogServerTest {

    @Mock
    private SyslogService syslogService;

    @Spy
    private SyslogConfiguration configuration = new SyslogConfiguration();

    private SyslogTestHelper helper;

    @InjectMocks
    private SyslogServer serverService;

    @BeforeEach
    void setup() {
        helper = new SyslogTestHelper(configuration);
        configuration.setTcpPort(helper.getNextPort());
        configuration.setUdpPort(helper.getNextPort());
        serverService.initialize();
    }

    @AfterEach
    void destroy() {
        serverService.destroy();
    }

    @Test
    void initialize() {
        assertNotNull(serverService);
    }

    @Test
    void sendTcp() throws IOException {
        helper.setProtocol("tcp");
        helper.sendLogs();
        sleepSeconds(1);
        assertEvents();
    }

    @Test
    void sendUdp() throws IOException {
        helper.setProtocol("udp");
        helper.sendLogs();
        sleepSeconds(2);
        assertEvents();
    }

    private void assertEvents() {
        ArgumentCaptor<net.microfalx.heimdall.protocol.syslog.SyslogMessage> syslogCapture = ArgumentCaptor.forClass(net.microfalx.heimdall.protocol.syslog.SyslogMessage.class);
        Mockito.verify(syslogService, Mockito.times(1)).accept(syslogCapture.capture());
        Iterator<net.microfalx.heimdall.protocol.syslog.SyslogMessage> iterator = syslogCapture.getAllValues().iterator();
        net.microfalx.heimdall.protocol.syslog.SyslogMessage message = iterator.next();
        assertEquals("", message.getName());
        assertEquals("myhost", message.getTargets().stream().findAny().get().getValue());
    }


}