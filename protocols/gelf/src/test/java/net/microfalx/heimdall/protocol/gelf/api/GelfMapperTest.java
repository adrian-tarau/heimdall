package net.microfalx.heimdall.protocol.gelf.api;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.gelf.jpa.GelfEvent;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MimeType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GelfMapperTest {

    private final RestApiMapper<GelfEvent, GelfDTO> gelfMapper = new GelfMapper();

    @Test
    void toDto() throws IOException {
        GelfEvent gelfEvent= new GelfEvent();
        gelfEvent.setId(1L);
        gelfEvent.setReceivedAt(LocalDateTime.now());
        gelfEvent.setSentAt(LocalDateTime.now());
        gelfEvent.setCreatedAt(LocalDateTime.now());
        gelfEvent.setLogger(StringUtils.EMPTY_STRING);
        gelfEvent.setThread(StringUtils.EMPTY_STRING);
        gelfEvent.setProcess(StringUtils.EMPTY_STRING);
        gelfEvent.setApplication(StringUtils.EMPTY_STRING);
        gelfEvent.setLevel(Severity.INFORMATIONAL);
        gelfEvent.setFacility(Facility.AUDIT);
        gelfEvent.setVersion("1.1");

        Part shortMessage= new Part();
        shortMessage.setId(1);
        shortMessage.setName("short");
        shortMessage.setResource("short test message");
        shortMessage.setType(net.microfalx.heimdall.protocol.core.Part.Type.BODY);
        shortMessage.setLength("short test message".length());
        shortMessage.setMimeType(MimeType.TEXT_PLAIN);
        shortMessage.setCreatedAt(LocalDateTime.now());
        gelfEvent.setShortMessage(shortMessage);

        Part longMessage= new Part();
        longMessage.setId(1);
        longMessage.setName("long");
        longMessage.setResource("long test message");
        longMessage.setType(net.microfalx.heimdall.protocol.core.Part.Type.BODY);
        longMessage.setLength("long test message".length());
        longMessage.setMimeType(MimeType.TEXT_PLAIN);
        longMessage.setCreatedAt(LocalDateTime.now());
        gelfEvent.setLongMessage(longMessage);

        Part fields= new Part();
        fields.setId(1);
        fields.setName("fields");
        fields.setResource("field content");
        fields.setType(net.microfalx.heimdall.protocol.core.Part.Type.BODY);
        fields.setLength("field content".length());
        fields.setMimeType(MimeType.TEXT_PLAIN);
        fields.setCreatedAt(LocalDateTime.now());
        gelfEvent.setFields(longMessage);

        net.microfalx.heimdall.protocol.core.jpa.Address address= new net.microfalx.heimdall.protocol.core.jpa.Address();
        address.setName("localhost");
        address.setCreatedAt(LocalDateTime.now());
        address.setModifiedAt(LocalDateTime.now());
        address.setType(Address.Type.HOSTNAME);
        address.setValue(InetAddress.getLocalHost().getHostAddress());
        address.setId(1);
        gelfEvent.setHost(address);
        GelfDTO gelfDTO = gelfMapper.toDto(gelfEvent);

        assertEquals(gelfEvent.getApplication(), gelfDTO.getApplication());
        assertEquals(gelfEvent.getId(), gelfDTO.getId());
        assertEquals(gelfEvent.getLogger(), gelfDTO.getLogger());
        assertEquals(gelfEvent.getFacility(), gelfDTO.getFacility());
        assertEquals(gelfEvent.getLevel(), gelfDTO.getSeverity());
        assertEquals(gelfEvent.getReceivedAt(), gelfDTO.getReceivedAt());
        assertEquals(gelfEvent.getSentAt(), gelfDTO.getSentAt());
        assertEquals(gelfEvent.getVersion(), gelfDTO.getVersion());
        assertEquals(gelfEvent.getApplication(), gelfDTO.getApplication());

        assertEquals(gelfEvent.getHost().getValue(), gelfDTO.getSource().getValue());
        assertEquals(gelfEvent.getHost().getName(),gelfDTO.getSource().getName());
        assertEquals(gelfEvent.getHost().getType(), gelfDTO.getSource().getType());

        assertEquals(gelfEvent.getFields().getResource().length(), gelfDTO.getFields().getResource().length());
        assertEquals(gelfEvent.getFields().getResource(), gelfDTO.getFields().getResource().loadAsString());
        assertEquals(gelfEvent.getFields().getFileName(), gelfDTO.getFields().getFileName());
        assertEquals(gelfEvent.getFields().getId(), (int) gelfDTO.getId());
        assertEquals(gelfEvent.getFields().getType(), gelfDTO.getFields().getType());
        assertEquals(gelfEvent.getFields().getMimeType(), MimeType.get(gelfDTO.getFields().getMimeType()));

        assertEquals(gelfEvent.getShortMessage().getResource().length(), gelfDTO.getShortMessage().getResource().length());
        assertEquals(gelfEvent.getShortMessage().getResource(), gelfDTO.getShortMessage().getResource().loadAsString());
        assertEquals(gelfEvent.getShortMessage().getFileName(), gelfDTO.getShortMessage().getFileName());
        assertEquals(gelfEvent.getShortMessage().getId(), (int) gelfDTO.getId());
        assertEquals(gelfEvent.getShortMessage().getType(), gelfDTO.getShortMessage().getType());
        assertEquals(gelfEvent.getShortMessage().getMimeType(), MimeType.get(gelfDTO.getShortMessage().getMimeType()));

        assertEquals(gelfEvent.getLongMessage().getResource().length(), gelfDTO.getLongMessage().getResource().length());
        assertEquals(gelfEvent.getLongMessage().getResource(), gelfDTO.getLongMessage().getResource().loadAsString());
        assertEquals(gelfEvent.getLongMessage().getFileName(), gelfDTO.getLongMessage().getFileName());
        assertEquals(gelfEvent.getLongMessage().getId(), (int) gelfDTO.getId());
        assertEquals(gelfEvent.getLongMessage().getType(), gelfDTO.getLongMessage().getType());
        assertEquals(gelfEvent.getLongMessage().getMimeType(), MimeType.get(gelfDTO.getLongMessage().getMimeType()));
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