package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.protocol.core.Attachment;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.gelf.jpa.GelfEventRepository;
import net.microfalx.heimdall.protocol.gelf.simulator.GelfSimulator;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;

@Service
public final class GelfService extends ProtocolService<GelfEvent, net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent> {

    @Autowired
    private GelfEventRepository eventRepository;

    @Autowired
    private GelfSimulator simulator;

    @Autowired
    private GelfProperties properties;

    @Override
    protected Event.Type getEventType() {
        return Event.Type.GELF;
    }

    @Override
    protected String getControllerPath() {
        return "/protocol/gelf";
    }

    @Override
    protected boolean isSimulatorEnabled(boolean enabled) {
        if (super.isSimulatorEnabled(enabled)) {
            return true;
        } else {
            return properties.isSimulatorEnabled();
        }
    }

    @Override
    protected void prepare(GelfEvent event) {
        lookupAddress(event.getSource());
    }

    protected void persist(GelfEvent message) {
        net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent gelfEvent = new net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent();
        gelfEvent.setFacility(message.getFacility());
        gelfEvent.setLevel(message.getGelfSeverity());
        gelfEvent.setVersion(message.getVersion());
        gelfEvent.setHost(lookupAddress(message.getSource()));
        gelfEvent.setCreatedAt(message.getCreatedAt().toLocalDateTime());
        gelfEvent.setReceivedAt(message.getReceivedAt().toLocalDateTime());
        gelfEvent.setSentAt(message.getSentAt().toLocalDateTime());
        gelfEvent.setApplication(message.getApplication());
        gelfEvent.setProcess(message.getProcess());
        gelfEvent.setLogger(message.getLogger());
        gelfEvent.setThread(message.getThread());
        Iterator<net.microfalx.heimdall.protocol.core.Part> parts = message.getParts().iterator();
        gelfEvent.setShortMessage(persistPart(parts.next()));
        if (parts.hasNext()) gelfEvent.setLongMessage(persistPart(parts.next()));
        Attachment fields = Attachment.create(message.toJson());
        message.addPart(fields);
        gelfEvent.setFields(persistPart(fields));
        eventRepository.save(gelfEvent);
        updateReference(message, gelfEvent.getId());
    }

    @Override
    protected GelfSimulator getSimulator() {
        return simulator;
    }

    @Override
    protected Resource getAttributesResource(net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent model) {
        return ResourceFactory.resolve(model.getFields().getResource()).withMimeType(MimeType.APPLICATION_JSON);
    }
}
