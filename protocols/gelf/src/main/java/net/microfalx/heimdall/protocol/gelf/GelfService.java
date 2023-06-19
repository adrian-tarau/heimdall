package net.microfalx.heimdall.protocol.gelf;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.jpa.GelfEvent;
import net.microfalx.heimdall.protocol.jpa.GelfEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

@Service
public class GelfService extends ProtocolService<GelfMessage> {

    @Autowired
    private GelfEventRepository gelfEventRepository;

    @Autowired
    private GelfSimulator gelfSimulator;

    /**
     * Handle one GELF message.
     *
     * @param message the GELF message
     */
    public void handle(GelfMessage message) throws IOException {
        GelfEvent gelfEvent = new GelfEvent();
        gelfEvent.setFacility(message.getFacility().numericalCode());
        gelfEvent.setLevel(message.getSeverity().getLevel());
        gelfEvent.setVersion(message.getVersion());
        gelfEvent.setAddress(lookupAddress(message.getSource()));
        gelfEvent.setReceivedAt(message.getReceivedAt().toLocalDateTime());
        gelfEvent.setSentAt(message.getSentAt().toLocalDateTime());
        gelfEvent.setFields(encodeFields(message));
        Iterator<net.microfalx.heimdall.protocol.core.Part> parts = message.getParts().iterator();
        gelfEvent.setShortMessage(persistPart(parts.next()));
        if (parts.hasNext()) {
            gelfEvent.setLongMessage(persistPart(parts.next()));
        }
        gelfEventRepository.save(gelfEvent);
    }

    @Override
    protected GelfSimulator getSimulator() {
        return gelfSimulator;
    }

    private String encodeFields(GelfMessage gelfMessage) {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            objectMapper.writeValue(writer, gelfMessage.getAttributes());
        } catch (IOException e) {
            // It will never happen
        }
        return writer.toString();
    }


}
