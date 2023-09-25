package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEventRepository;
import org.snmp4j.PDU;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class SnmpService extends ProtocolService<SnmpEvent> {

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

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SNMP;
    }

    protected void persist(SnmpEvent trap) {
        net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent snmpEvent = new net.
                microfalx.heimdall.protocol.snmp.jpa.SnmpEvent();
        Address address = Address.create(trap.getSource().getType(),
                trap.getSource().getName(), trap.getSource().getValue());
        snmpEvent.setAgentAddress(lookupAddress(address));
        snmpEvent.setCreatedAt(trap.getCreatedAt().toLocalDateTime());
        snmpEvent.setSentAt(trap.getSentAt().toLocalDateTime());
        snmpEvent.setReceivedAt(trap.getReceivedAt().toLocalDateTime());
        snmpEvent.setVersion(trap.getVersion());

        snmpEvent.setCommunityString(trap.getCommunity());
        snmpEvent.setEnterprise(trap.getEnterprise());
        snmpEvent.setTrapType(PDU.TRAP);

        snmpEvent.setMessage(persistPart(trap.getBody()));
        trap.setBody(Body.create(encodeAttributes(trap)));
        snmpEvent.setBindingPart(persistPart(trap.getBody()));

        repository.save(snmpEvent);
    }
}
