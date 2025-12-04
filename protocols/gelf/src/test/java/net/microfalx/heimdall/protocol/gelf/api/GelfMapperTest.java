package net.microfalx.heimdall.protocol.gelf.api;

import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MimeType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GelfMapperTest {

    private final RestApiMapper<GelfEvent, GelfDTO> gelfMapper = new GelfMapper();

    @Test
    void toDto() {

    }

    @Test
    void toEntity() throws IOException {
        GelfDTO gelfDTO = new GelfDTO();
        gelfDTO.setShortMessage(Body.create("short test message"));
        gelfDTO.setLongMessage(Body.create("long test message"));
        gelfDTO.setSource(Address.local());
        gelfDTO.setLogger(StringUtils.EMPTY_STRING);
        gelfDTO.setThread(StringUtils.EMPTY_STRING);
        gelfDTO.setProcess(StringUtils.EMPTY_STRING);
        gelfDTO.setName("test Gelf");
        gelfDTO.setSentAt(LocalDateTime.now());
        gelfDTO.setReceivedAt(LocalDateTime.now());
        gelfDTO.setApplication(StringUtils.EMPTY_STRING);
        gelfDTO.setFields(Body.create("test field"));
        gelfDTO.setId(1L);

        GelfEvent entity = gelfMapper.toEntity(gelfDTO);

        assertEquals(gelfDTO.getApplication(), entity.getApplication());
        assertEquals(gelfDTO.getId(), entity.getId());
        assertEquals(gelfDTO.getLogger(), entity.getLogger());
        assertEquals(gelfDTO.getFacility(), entity.getFacility());
        assertEquals(gelfDTO.getSeverity(), entity.getLevel());
        assertEquals(gelfDTO.getCreatedAt(), entity.getCreatedAt());
        assertEquals(gelfDTO.getReceivedAt(), entity.getReceivedAt());
        assertEquals(gelfDTO.getSentAt(), entity.getSentAt());
        assertEquals(gelfDTO.getVersion(), entity.getVersion());
        assertEquals(gelfDTO.getApplication(), entity.getApplication());

        assertEquals(gelfDTO.getSource().getValue(), entity.getHost().getValue());
        assertEquals(gelfDTO.getSource().getName(), entity.getHost().getName());
        assertEquals(gelfDTO.getSource().getType(), entity.getHost().getType());

        assertEquals(gelfDTO.getFields().getResource().length(), entity.getFields().getLength());
        assertEquals(gelfDTO.getFields().loadAsString(), entity.getFields().getResource());
        assertEquals(gelfDTO.getFields().getFileName(), entity.getFields().getFileName());
        assertEquals(gelfDTO.getFields().getName(), entity.getFields().getName());
        assertEquals(gelfDTO.getId(), (long) entity.getFields().getId());
        assertEquals(gelfDTO.getFields().getType(), entity.getFields().getType());
        assertEquals(MimeType.get(gelfDTO.getFields().getMimeType()), entity.getFields().getMimeType());

        assertEquals(gelfDTO.getShortMessage().getResource().length(), entity.getShortMessage().getLength());
        assertEquals(gelfDTO.getShortMessage().loadAsString(), entity.getShortMessage().getResource());
        assertEquals(gelfDTO.getShortMessage().getFileName(), entity.getShortMessage().getFileName());
        assertEquals(gelfDTO.getShortMessage().getName(), entity.getShortMessage().getName());
        assertEquals(gelfDTO.getId(), (long) entity.getShortMessage().getId());
        assertEquals(gelfDTO.getFields().getType(), entity.getShortMessage().getType());
        assertEquals(MimeType.get(gelfDTO.getShortMessage().getMimeType()), entity.getShortMessage().getMimeType());

        assertEquals(gelfDTO.getLongMessage().getResource().length(), entity.getLongMessage().getLength());
        assertEquals(gelfDTO.getLongMessage().loadAsString(), entity.getLongMessage().getResource());
        assertEquals(gelfDTO.getLongMessage().getFileName(), entity.getLongMessage().getFileName());
        assertEquals(gelfDTO.getLongMessage().getName(), entity.getLongMessage().getName());
        assertEquals(gelfDTO.getId(), (long) entity.getLongMessage().getId());
        assertEquals(gelfDTO.getLongMessage().getType(), entity.getLongMessage().getType());
        assertEquals(MimeType.get(gelfDTO.getLongMessage().getMimeType()), entity.getLongMessage().getMimeType());

    }
}