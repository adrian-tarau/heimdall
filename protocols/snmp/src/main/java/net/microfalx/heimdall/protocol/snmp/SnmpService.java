package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEventRepository;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.snmp4j.PDU;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class SnmpService extends ProtocolService<SnmpEvent, net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent> {

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

    @Override
    protected void prepare(SnmpEvent event) {
        lookupAddress(event.getSource());
    }

    protected void persist(SnmpEvent event) {
        net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent snmpEvent = new net.
                microfalx.heimdall.protocol.snmp.jpa.SnmpEvent();
        snmpEvent.setAgentAddress(lookupAddress(event.getSource()));
        snmpEvent.setCreatedAt(event.getCreatedAt().toLocalDateTime());
        snmpEvent.setSentAt(event.getSentAt().toLocalDateTime());
        snmpEvent.setReceivedAt(event.getReceivedAt().toLocalDateTime());
        snmpEvent.setVersion(event.getVersion());

        snmpEvent.setCommunityString(event.getCommunity());
        snmpEvent.setEnterprise(event.getEnterprise());
        snmpEvent.setTrapType(PDU.TRAP);

        snmpEvent.setMessage(persistPart(event.getBody()));
        event.setBody(Body.create(event.toJson()));
        snmpEvent.setBindingPart(persistPart(event.getBody()));

        repository.save(snmpEvent);
    }

    @Override
    protected Resource getAttributesResource(net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent model) {
        return ResourceFactory.resolve(model.getBindingPart().getResource()).withMimeType(MimeType.APPLICATION_JSON);
    }

}
