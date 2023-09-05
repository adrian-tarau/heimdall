package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.bootstrap.test.AbstractBootstrapServiceTestCase;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolSimulatorProperties;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEventRepository;
import net.microfalx.resource.FileResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {ProtocolSimulatorProperties.class, SnmpProperties.class, SnmpService.class})
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
    void handle() throws IOException {
        ArgumentCaptor<net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent> snmpEventArgumentCaptor =
                ArgumentCaptor.forClass(net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent.class);
        snmpService.accept(snmpEvent);
        Mockito.verify(snmpEventRepository).save(snmpEventArgumentCaptor.capture());
        assertEquals(snmpEvent.getSource().getValue(), snmpEventArgumentCaptor.getValue().getAgentAddress().getValue());
        assertEquals(snmpEvent.getSource().getName(), snmpEventArgumentCaptor.getValue().getAgentAddress().getName());
        assertEquals(snmpEvent.getSource().getType(), snmpEventArgumentCaptor.getValue().getAgentAddress().getType());
        assertEquals(snmpEvent.getCommunity(), snmpEventArgumentCaptor.getValue().getCommunityString());
        assertEquals(snmpEvent.getEnterprise(), snmpEventArgumentCaptor.getValue().getEnterprise());
        assertEquals(String.valueOf(snmpEvent.getVersion()), snmpEventArgumentCaptor.getValue().getVersion());
        assertEquals(snmpEvent.getBodyAsString(), FileResource.create(URI.create(snmpEventArgumentCaptor.getValue().getBindingPart().getResource())).loadAsString());
        assertEquals(snmpEvent.getCreatedAt().toLocalDateTime(), snmpEventArgumentCaptor.getValue().getCreatedAt());
        assertEquals(snmpEvent.getReceivedAt().toLocalDateTime(), snmpEventArgumentCaptor.getValue().getReceivedAt());
        assertEquals(snmpEvent.getSentAt().toLocalDateTime(), snmpEventArgumentCaptor.getValue().getSentAt());
        assertEquals("SNMP Trap", snmpEvent.getName());
    }
}
