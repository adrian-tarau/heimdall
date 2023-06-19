package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.ProtocolClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Iterator;

import static net.microfalx.lang.ThreadUtils.sleepSeconds;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GelfServerTest {

    @Mock
    private GelfService syslogService;

    @Spy
    private GelfConfiguration configuration = new GelfConfiguration();

    private GelfTestHelper helper;

    @InjectMocks
    private GelfServer serverService;

    @BeforeEach
    void setup() {
        helper = new GelfTestHelper(configuration);
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
        helper.setTransport(ProtocolClient.Transport.TCP);
        helper.sendLogs(false);
        sleepSeconds(1);
        assertEvents(false);
    }

    @Test
    void sendUdpSmall() throws IOException {
        helper.setTransport(ProtocolClient.Transport.UDP);
        helper.sendLogs(false);
        sleepSeconds(2);
        assertEvents(false);
    }

    @Test
    void sendUdpLarge() throws IOException {
        helper.setTransport(ProtocolClient.Transport.UDP);
        helper.sendLogs(true);
        sleepSeconds(5);
        assertEvents(true);
    }

    void assertEvents(boolean large) throws IOException {
        ArgumentCaptor<GelfEvent> gelfCapture =
                ArgumentCaptor.forClass(GelfEvent.class);
        Mockito.verify(syslogService, Mockito.times(1)).handle(gelfCapture.capture());
        Iterator<GelfEvent> iterator = gelfCapture.getAllValues().iterator();
        GelfEvent message = iterator.next();
        assertEquals("Gelf Message", message.getName());
        if (!large) {
            assertEquals("Test message", message.getParts().stream().findFirst().
                    get().getResource().loadAsString());
            assertEquals("Test message", message.getParts().stream().toList().
                    get(1).getResource().loadAsString());
        } else {
            assertTrue(message.getParts().stream().findFirst().get().getResource().loadAsString().length() > 5000);
        }
        assertNotNull(message.getSource().getValue());
        assertNotNull(message.getCreatedAt());
        assertNotNull(message.getSentAt());
        assertNotNull(message.getReceivedAt());
        assertEquals("LOCAL1", message.getFacility().label());
        assertEquals(Severity.CRITICAL, message.getGelfSeverity());
        assertEquals(net.microfalx.heimdall.protocol.core.Severity.ERROR, message.getSeverity());
    }

}