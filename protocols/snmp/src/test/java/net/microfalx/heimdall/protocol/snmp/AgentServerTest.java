package net.microfalx.heimdall.protocol.snmp;

import org.assertj.core.api.Assertions;
import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.MOScope;
import org.snmp4j.agent.ManagedObject;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static net.microfalx.heimdall.protocol.snmp.SnmpUtils.*;

@ExtendWith(MockitoExtension.class)
class AgentServerTest extends AbstractSnmpServiceTestCase {

    @InjectMocks
    private AgentServer agentServer;

    private Snmp snmp;

    @BeforeEach
    @Override
    void setup() throws Exception {
        super.setup();
        initClient();
        Reflect.on(agentServer).set("snmpService", snmpService);
        agentServer.afterPropertiesSet();
    }

    @Test
    void listManagedObjects() {
        Iterator<Map.Entry<MOScope, ManagedObject<?>>> managedObjects = agentServer.getManagedObjects();
        while (managedObjects.hasNext()) {
            Map.Entry<MOScope, ManagedObject<?>> entry = managedObjects.next();
            System.out.println(describeScope(entry.getKey())
                    + "\t\t" + describeMoType(entry.getValue())
                    + "\t\t" + describeMoValue(entry.getValue(), true));
        }
    }

    @Test
    void getSystemDescription() throws IOException {
        String value = getAsString(SnmpConstants.sysDescr);
        Assertions.assertThat(value).contains("SNMP4J-Agent");
    }

    @Test
    void getBulk() throws IOException {
        String[] values = getBulkAsString(new OID("1.3.6.1.2.1.1"), 10);
        Assertions.assertThat(values).hasSizeGreaterThan(5);
    }

    public String getAsString(OID oid) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        ResponseEvent<Address> event = snmp.send(pdu, createCommunityTarget());
        return getValueAsString(getResponsePdu(event));
    }

    public String[] getBulkAsString(OID oid, int max) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GETBULK);
        pdu.setMaxRepetitions(max);
        pdu.setNonRepeaters(0);
        ResponseEvent<Address> event = snmp.send(pdu, createCommunityTarget());
        return getValueAsStrings(getResponsePdu(event));
    }

    private String getValueAsString(PDU pdu) {
        return getValueAsString(pdu, 0);
    }

    private String getValueAsString(PDU pdu, int index) {
        VariableBinding vb = pdu.get(index);
        return vb.getVariable().toString();
    }

    private String[] getValueAsStrings(PDU pdu) {
        return pdu.getAll().stream()
                .map(vb -> vb.getVariable().toString())
                .toList().toArray(new String[0]);
    }

    private PDU getResponsePdu(ResponseEvent<Address> responseEvent) {
        if (responseEvent != null && responseEvent.getResponse() != null) {
            PDU response = responseEvent.getResponse();
            if (response.getErrorStatus() == PDU.noError) {
                return response;
            } else {
                throw new SnmpException("Error: " + response.getErrorStatusText());
            }
        }
        throw new SnmpException("Timeout or no response from agent");
    }

    private CommunityTarget<Address> createCommunityTarget() {
        CommunityTarget<Address> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(properties.getAgentComunityString()));
        target.setAddress(GenericAddress.parse("udp:localhost/" + properties.getUdpPort()));
        target.setRetries(1);
        target.setTimeout(5_000);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    private void initClient() throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

}