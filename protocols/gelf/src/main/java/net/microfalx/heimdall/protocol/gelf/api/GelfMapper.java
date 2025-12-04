package net.microfalx.heimdall.protocol.gelf.api;

import net.microfalx.bootstrap.restapi.AbstractRestApiMapper;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.MimeType;

import java.time.LocalDateTime;

public class GelfMapper extends AbstractRestApiMapper<GelfEvent, GelfDTO> {

    @Override
    protected GelfDTO doToDto(GelfEvent gelfEvent) {
        GelfDTO gelfDTO = new GelfDTO();
        gelfDTO.setSeverity(gelfEvent.getLevel());
        gelfDTO.setFacility(gelfEvent.getFacility());
        gelfDTO.setId(gelfEvent.getId());
        gelfDTO.setApplication(gelfEvent.getApplication());
        gelfDTO.setName(gelfEvent.getLongMessage().getName());
        gelfDTO.setVersion(gelfEvent.getVersion());
        gelfDTO.setProcess(gelfEvent.getProcess());
        gelfDTO.setLogger(gelfEvent.getLogger());
        gelfDTO.setReceivedAt(gelfEvent.getReceivedAt());
        gelfDTO.setSentAt(gelfEvent.getSentAt());
        gelfDTO.setSource(Address.create(gelfEvent.getHost().getType(), gelfEvent.getHost().getValue(), gelfEvent.getHost().getName()));
        gelfDTO.setShortMessage(Body.create(gelfEvent.getShortMessage().getResource()));
        gelfDTO.setLongMessage(Body.create(gelfEvent.getLongMessage().getResource()));
        gelfDTO.setFields(Body.create(gelfEvent.getFields().getResource()));
        return gelfDTO;
    }

    @Override
    protected GelfEvent doToEntity(GelfDTO gelfDTO) {
        GelfEvent gelfEvent = new GelfEvent();
        try {
            gelfEvent.setApplication(gelfDTO.getApplication());
            gelfEvent.setLogger(gelfDTO.getLogger());
            gelfEvent.setId(gelfDTO.getId());
            net.microfalx.heimdall.protocol.core.jpa.Address address = new net.microfalx.heimdall.protocol.core.jpa.Address();
            address.setValue(gelfDTO.getSource().getValue());
            address.setId((int) gelfDTO.getId());
            address.setType(gelfDTO.getSource().getType());
            address.setCreatedAt(gelfDTO.getCreatedAt());
            address.setName(gelfDTO.getSource().getName());
            gelfEvent.setHost(address);

            gelfEvent.setProcess(gelfDTO.getProcess());
            gelfEvent.setVersion(gelfDTO.getVersion());
            gelfEvent.setThread(gelfDTO.getThread());
            gelfEvent.setLevel(gelfDTO.getSeverity());
            gelfEvent.setFacility(gelfDTO.getFacility());
            gelfEvent.setReceivedAt(gelfDTO.getReceivedAt());
            gelfEvent.setCreatedAt(gelfDTO.getCreatedAt());
            gelfEvent.setSentAt(gelfDTO.getSentAt());

            Part fields = new Part();
            fields.setId((int) gelfDTO.getId());
            fields.setLength((int) gelfDTO.getFields().getResource().length());
            fields.setType(gelfDTO.getFields().getType());
            fields.setName(gelfDTO.getFields().getName());
            fields.setCreatedAt(LocalDateTime.now());
            fields.setFileName(gelfDTO.getFields().getFileName());
            fields.setMimeType(MimeType.get(gelfDTO.getFields().getMimeType()));
            fields.setResource(gelfDTO.getFields().loadAsString());
            gelfEvent.setFields(fields);

            Part longMessage = new Part();
            longMessage.setId((int) gelfDTO.getId());
            longMessage.setResource(gelfDTO.getLongMessage().loadAsString());
            longMessage.setType(gelfDTO.getLongMessage().getType());
            longMessage.setMimeType(MimeType.get(gelfDTO.getLongMessage().getMimeType()));
            longMessage.setName(gelfDTO.getLongMessage().getName());
            longMessage.setLength((int) gelfDTO.getLongMessage().getResource().length());
            longMessage.setFileName(gelfDTO.getLongMessage().getFileName());
            longMessage.setCreatedAt(LocalDateTime.now());
            gelfEvent.setLongMessage(longMessage);

            Part shortMessage = new Part();
            shortMessage.setResource(gelfDTO.getShortMessage().loadAsString());
            shortMessage.setType(gelfDTO.getShortMessage().getType());
            shortMessage.setMimeType(MimeType.get(gelfDTO.getShortMessage().getMimeType()));
            shortMessage.setName(gelfDTO.getShortMessage().getName());
            shortMessage.setLength((int) gelfDTO.getShortMessage().getResource().length());
            shortMessage.setId((int)gelfDTO.getId());
            shortMessage.setFileName(gelfDTO.getShortMessage().getFileName());
            shortMessage.setCreatedAt(LocalDateTime.now());
            gelfEvent.setShortMessage(shortMessage);
        } catch (Exception e) {
            ExceptionUtils.rethrowException(e);
        }
        return gelfEvent;
    }
}
