package net.microfalx.heimdall.protocol.snmp;

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
class SnmpServerTest {

    @Mock
    private SnmpService snmpService;

    @Spy
    private SnmpProperties configuration = new SnmpProperties();

    private SnmpTestHelper helper;

    @InjectMocks
    private SnmpServer snmpServer;

    @BeforeEach
    void setup() {
        helper = new SnmpTestHelper(configuration);
        configuration.setTcpPort(helper.getNextPort());
        configuration.setUdpPort(helper.getNextPort());
        snmpServer.initialize();
    }

    @AfterEach
    void destroy() {
        snmpServer.destroy();
    }

    @Test
    void initialize() {
        assertNotNull(snmpServer);
    }

    @Test
    void sendSmall() throws IOException {
        helper.sendTrap(false);
        sleepSeconds(1);
    }

    @Test
    void sendLarge() throws IOException {
        helper.sendTrap(false);
        sleepSeconds(1);
    }

}