package net.microfalx.heimdall.protocol.snmp;

import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.snmp4j.mp.SnmpConstants;

import java.io.IOException;

import static net.microfalx.lang.ThreadUtils.sleepSeconds;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TrapServerTest extends AbstractSnmpServiceTestCase {

    @InjectMocks
    private TrapServer trapServer;

    @BeforeEach
    @Override
    void setup() throws Exception {
        super.setup();
        Reflect.on(trapServer).set("snmpService", snmpService);
        trapServer.afterPropertiesSet();
    }

    @Test
    void initialize() {
        assertNotNull(trapServer);
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