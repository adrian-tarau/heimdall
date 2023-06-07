package net.microfalx.heimdall.protocol.syslog;

import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.jpa.SyslogEvent;
import net.microfalx.heimdall.protocol.jpa.SyslogEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SyslogService extends ProtocolService<SyslogMessage> {

    @Autowired
    private SyslogEventRepository syslogEventRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PartRepository partRepository;

    public void handle(SyslogMessage message) {
        SyslogEvent syslogEvent = new SyslogEvent();
        syslogEvent.setFacility(message.getFacility().numericalCode());
        syslogEvent.setSeverity(message.getSeverity().numericalCode());
        syslogEvent.setAddress(lookupAddress(message.getSource()));
        syslogEvent.setReceivedAt(message.getReceivedAt().toLocalDateTime());
        syslogEvent.setSentAt(message.getSentAt().toLocalDateTime());
        syslogEvent.setPart(saveMessage(message));
        syslogEventRepository.save(syslogEvent);
    }

    private Part saveMessage(SyslogMessage message) {
        Body body = message.getBody();
        Part part = new Part();
        part.setType(body.getType());
        part.setResource(body.getResource().toURI().toASCIIString());
        part.setCreatedAt(message.getCreatedAt().toLocalDateTime());
        part.setName(message.getName());
        partRepository.save(part);
        return part;
    }


}
