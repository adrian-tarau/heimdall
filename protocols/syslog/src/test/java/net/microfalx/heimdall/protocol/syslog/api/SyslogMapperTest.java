package net.microfalx.heimdall.protocol.syslog.api;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEvent;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import org.apache.kafka.common.message.ReadShareGroupStateSummaryRequestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SyslogMapperTest {

    private final RestApiMapper<SyslogEvent,SyslogDTO> syslogMapper = new SyslogMapper();

    @Test
    void toDto() throws IOException {
        SyslogEvent syslogEvent = new SyslogEvent();
        syslogEvent.setId(1L);
        syslogEvent.setFacility(Facility.AUDIT);
        syslogEvent.setSeverity(Severity.INFORMATIONAL);
        syslogEvent.setSentAt(LocalDateTime.now());
        syslogEvent.setReceivedAt(LocalDateTime.now());

        net.microfalx.heimdall.protocol.core.jpa.Address address = new net.microfalx.heimdall.protocol.core.jpa.Address();
        address.setId(1);
        address.setValue("12.150.135.182");
        address.setType(Address.Type.HOSTNAME);
        syslogEvent.setAddress(address);

        Part part = new Part();
        part.setId(1);
        part.setName("Test Part");
        part.setResource("test message");
        part.setLength("test message".length());
        part.setType(net.microfalx.heimdall.protocol.core.Part.Type.BODY);
        part.setMimeType(MimeType.TEXT_PLAIN);
        syslogEvent.setMessage(part);

        SyslogDTO syslogDTO = syslogMapper.toDto(syslogEvent);

        assertEquals(syslogEvent.getId(), syslogDTO.getId());
        assertEquals(syslogEvent.getFacility(), syslogDTO.getFacility());
        assertEquals(syslogEvent.getSeverity(), syslogDTO.getSeverity());
        assertEquals(syslogEvent.getMessage().getName(), syslogDTO.getName());
        assertNotNull(syslogDTO.getSentAt());
        assertNotNull(syslogDTO.getReceivedAt());


        assertNull(syslogEvent.getAddress().getName());
        assertEquals(syslogEvent.getAddress().getValue(), syslogDTO.getSource().getValue());
        assertEquals(syslogEvent.getAddress().getType(), syslogDTO.getSource().getType());

        assertEquals(syslogEvent.getMessage().getMimeType().getValue(), syslogDTO.getMessage().getMimeType());
        assertEquals(syslogEvent.getMessage().getFileName(), syslogDTO.getMessage().getFileName());
        assertEquals(syslogEvent.getMessage().getLength(),syslogDTO.getMessage().getResource().length());
        assertEquals(syslogEvent.getMessage().getResource(), syslogDTO.getMessage().getResource().loadAsString());
    }

    @Test
    void toEntity() throws IOException {
        SyslogDTO syslogDTO = new SyslogDTO();
        syslogDTO.setId(1);
        syslogDTO.setFacility(Facility.AUDIT);
        syslogDTO.setSeverity(Severity.INFORMATIONAL);
        syslogDTO.setMessage(Body.create("test message"));
        syslogDTO.setSource(Address.host("12.150.135.182"));

        SyslogEvent entity = syslogMapper.toEntity(syslogDTO);

        assertEquals(syslogDTO.getFacility(), entity.getFacility());
        assertEquals(syslogDTO.getSeverity(), entity.getSeverity());
        assertEquals(syslogDTO.getId(), entity.getId());

        assertEquals(syslogDTO.getSource().getName(), entity.getAddress().getName());
        assertEquals(syslogDTO.getSource().getType(), entity.getAddress().getType());
        assertEquals(syslogDTO.getSource().getValue(), entity.getAddress().getValue());

        assertEquals(syslogDTO.getMessage().getMimeType(), entity.getMessage().getMimeType().getValue());
        assertEquals(syslogDTO.getMessage().getFileName(), entity.getMessage().getFileName());
        assertEquals(syslogDTO.getId(), entity.getMessage().getId().longValue());
        assertEquals(syslogDTO.getMessage().getName(), entity.getMessage().getName());
        assertEquals(syslogDTO.getMessage().loadAsString(), entity.getMessage().getResource());
        assertEquals(syslogDTO.getMessage().getResource().length(), entity.getMessage().getLength());
    }
}