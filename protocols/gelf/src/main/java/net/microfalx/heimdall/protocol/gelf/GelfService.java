package net.microfalx.heimdall.protocol.gelf;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.jpa.GelfEvent;
import net.microfalx.heimdall.protocol.jpa.GelfEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Service
public class GelfService extends ProtocolService<GelfMessage> {

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private GelfEventRepository gelfEventRepository;

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
        Part shortAttachmentId = addMessage(message, 0);
        gelfEvent.setShort_attachment_id(shortAttachmentId);
        Part longAttachmentId = addMessage(message, 1);
        gelfEvent.setLong_attachment_id(longAttachmentId);
        gelfEventRepository.save(gelfEvent);
        partRepository.save(shortAttachmentId);
        partRepository.save(longAttachmentId);
    }

    private Part addMessage(GelfMessage message, int position) throws IOException {
        List<net.microfalx.heimdall.protocol.core.Part> parts = message.getParts().stream().toList();
        Part part = new Part();
        part.setType(parts.get(position).getType());
        part.setName(parts.get(position).getName());
        part.setResource(parts.get(position).getResource().loadAsString());
        part.setCreatedAt(parts.get(position).getEvent().getCreatedAt().toLocalDateTime());
        return part;
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
