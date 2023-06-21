package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolConfiguration;
import net.microfalx.heimdall.protocol.core.ProtocolSimulatorProperties;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;

@ContextConfiguration(classes = {ProtocolConfiguration.class, ProtocolSimulatorProperties.class,
        SnmpProperties.class, SnmpService.class})
public class SnmpServiceTest extends AbstractBootstrapServiceTestCase {

    @MockBean
    private AddressRepository addressRepository;

    @MockBean
    private PartRepository partRepository;

    @MockBean
    private SnmpEventRepository snmpEventRepository;

    @MockBean
    private SnmpSimulator snmpSimulator;

    @Autowired
    private SnmpService snmpService;

    private SnmpEvent snmpEvent = new SnmpEvent();

    @BeforeEach
    void setUp() throws UnknownHostException {
        snmpEvent.setVersion(SnmpConstants.version2c);
        snmpEvent.setReceivedAt(ZonedDateTime.now());
        snmpEvent.setCreatedAt(ZonedDateTime.now());
        snmpEvent.setSentAt(ZonedDateTime.now());
        snmpEvent.setName("SNMP Trap");
        snmpEvent.setSource(Address.create(Address.Type.HOSTNAME, InetAddress.getLocalHost().getHostAddress()
                , InetAddress.getLocalHost().getHostAddress()));
        snmpEvent.setEnterprise("dummy");
        snmpEvent.setCommunity("public");
        snmpEvent.setBody(Body.create("Test message"));
    }

    @Test
    void handle() {
        ArgumentCaptor<net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent> snmpEventArgumentCaptor =
                ArgumentCaptor.forClass(net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent.class);
        snmpService.handle(snmpEvent);
        Mockito.verify(snmpEventRepository).save(snmpEventArgumentCaptor.capture());
    }
}
