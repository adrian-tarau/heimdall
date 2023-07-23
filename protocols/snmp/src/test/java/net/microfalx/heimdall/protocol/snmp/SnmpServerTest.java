package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snmp4j.mp.SnmpConstants;

import java.io.IOException;

import static net.microfalx.lang.ThreadUtils.sleepSeconds;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnmpServerTest {

    @Mock
    private SnmpService snmpService;

    @Mock
    private MibService mibService;

    @Mock
    private MibVariable mibVariable;

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
        helper.sendTrap(true);
        sleepSeconds(1);
    }

    @Test
    void sendEventWithResolvedOID() throws IOException {
        when(mibVariable.getName()).thenReturn("Dummy Variable");
        when(mibService.findVariable(anyString())).thenReturn(mibVariable);
        helper.addAttribute("1.2.3", "1");
        helper.sendTrap(false);
        sleepSeconds(1);
        Mockito.verify(mibService).findVariable("1.2.3");
        Mockito.verify(mibService).findVariable(SnmpConstants.sysUpTime.toDottedString());
    }
}