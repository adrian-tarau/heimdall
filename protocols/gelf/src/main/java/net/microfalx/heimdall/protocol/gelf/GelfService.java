package net.microfalx.heimdall.protocol.gelf;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
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

    @Autowired
    private AddressRepository addressRepository;

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
        Part shortMessage = new Part();
        List<net.microfalx.heimdall.protocol.core.Part> parts = message.getParts().stream().toList();
        addMessage(parts, 0, shortMessage);
        addMessage(parts, 1, shortMessage);
        gelfEventRepository.save(gelfEvent);
    }

    private void addMessage(List<net.microfalx.heimdall.protocol.core.Part> parts, int index, Part message) throws IOException {
        net.microfalx.heimdall.protocol.core.Part part = parts.get(index);
        message.setType(part.getType());
        message.setResource(part.getResource().loadAsString());
        message.setName(part.getName());
        message.setCreatedAt(part.getEvent().getCreatedAt().toLocalDateTime());
        partRepository.save(message);
    }

    private String encodeFields(GelfMessage gelfMessage) {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            objectMapper.writeValue(writer, gelfMessage.getAttributes().keySet());
        } catch (IOException e) {
            // It will never happen
        }
        return writer.toString();
    }


}
