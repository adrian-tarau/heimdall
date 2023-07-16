package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEventRepository;
import net.microfalx.lang.StringUtils;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SnmpService extends ProtocolService<SnmpEvent> {

    @Autowired
    private SnmpSimulator simulator;

    @Autowired
    private SnmpProperties properties;

    @Autowired
    private SnmpEventRepository repository;

    @Override
    protected SnmpSimulator getSimulator() {
        return simulator;
    }

    protected void persist(SnmpEvent trap) {
        trap.setBody(Body.create(encodeAttributes(trap)));
        net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent snmpEvent = new net.
                microfalx.heimdall.protocol.snmp.jpa.SnmpEvent();
        Address address = Address.create(trap.getSource().getType(),
                trap.getSource().getName(), trap.getSource().getValue());
        snmpEvent.setAgentAddress(lookupAddress(address));
        snmpEvent.setCreatedAt(trap.getCreatedAt().toLocalDateTime());
        snmpEvent.setSentAt(trap.getSentAt().toLocalDateTime());
        snmpEvent.setReceivedAt(trap.getReceivedAt().toLocalDateTime());
        snmpEvent.setVersion(String.valueOf(trap.getVersion()));
        snmpEvent.setBindingPart(persistPart(trap.getBody()));
        snmpEvent.setCommunityString(StringUtils.defaultIfEmpty(trap.getCommunity(),
                String.valueOf(new OctetString("public"))));
        snmpEvent.setVersion(StringUtils.
                defaultIfEmpty(String.valueOf(trap.getVersion()), String.valueOf(SnmpConstants.version2c)));
        snmpEvent.setEnterprise(trap.getEnterprise());
        snmpEvent.setTrapType(PDU.TRAP);
        repository.save(snmpEvent);
    }
}
