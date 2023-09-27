package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.protocol.core.Attachment;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.gelf.jpa.GelfEventRepository;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;

@Service
public final class GelfService extends ProtocolService<GelfEvent, net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent> {

    @Autowired
    private GelfEventRepository gelfEventRepository;

    @Autowired
    private GelfSimulator gelfSimulator;

    @Autowired
    private GelfConfiguration gelfConfiguration;

    @Override
    protected Event.Type getEventType() {
        return Event.Type.GELF;
    }

    protected void persist(GelfEvent message) {
        net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent gelfEvent = new net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent();
        gelfEvent.setFacility(message.getFacility().numericalCode());
        gelfEvent.setLevel(message.getSeverity().getLevel());
        gelfEvent.setVersion(message.getVersion());
        gelfEvent.setAddress(lookupAddress(message.getSource()));
        gelfEvent.setCreatedAt(message.getCreatedAt().toLocalDateTime());
        gelfEvent.setReceivedAt(message.getReceivedAt().toLocalDateTime());
        gelfEvent.setSentAt(message.getSentAt().toLocalDateTime());
        Iterator<net.microfalx.heimdall.protocol.core.Part> parts = message.getParts().iterator();
        gelfEvent.setShortMessage(persistPart(parts.next()));
        if (parts.hasNext()) gelfEvent.setLongMessage(persistPart(parts.next()));
        Attachment fields = Attachment.create(message.toJson());
        message.addPart(fields);
        gelfEvent.setFields(persistPart(fields));
        gelfEventRepository.save(gelfEvent);
    }

    @Override
    protected GelfSimulator getSimulator() {
        return gelfSimulator;
    }

    @Override
    protected Resource getAttributesResource(net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent model) {
        return ResourceFactory.resolve(model.getFields().getResource()).withMimeType(MimeType.APPLICATION_JSON);
    }
}
