package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.protocol.core.ProtocolClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static net.microfalx.lang.ThreadUtils.sleepSeconds;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GelfServerServiceTest {

    @Mock
    private GelfService syslogService;

    @Spy
    private GelfConfiguration configuration = new GelfConfiguration();

    private GelfTestHelper helper;

    @InjectMocks
    private GelfServerService serverService;

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
        helper.sendLogs();
        sleepSeconds(1);
        //assertEvents();
    }

    @Test
    void sendUdp() throws IOException {
        helper.setTransport(ProtocolClient.Transport.UDP);
        helper.sendLogs();
        sleepSeconds(2);
        //assertEvents();
    }

}