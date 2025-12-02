package net.microfalx.heimdall.protocol.syslog.api;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEvent;
import org.apache.kafka.common.message.ReadShareGroupStateSummaryRequestData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class SyslogMapperTest {

    private final SyslogMapper syslogMapper = new SyslogMapper();

    @Test
    void toDto() {

    }

    @Test
    void toEntity() throws IOException {
        SyslogDTO syslogDTO = new SyslogDTO();
        syslogDTO.setId(1);
        syslogDTO.setFacility(Facility.AUDIT);
        syslogDTO.setSeverity(Severity.INFORMATIONAL);
        syslogDTO.setMessage(Body.create("test message"));
        syslogDTO.setAddress(Address.host("12.150.135.182"));

        SyslogEvent entity = syslogMapper.toEntity(syslogDTO);

        assertEquals(syslogDTO.getFacility(), entity.getFacility());
        assertEquals(syslogDTO.getSeverity(), entity.getSeverity());
        assertEquals(syslogDTO.getId(), entity.getId());

        assertEquals(syslogDTO.getAddress().getName(), entity.getAddress().getName());
        assertEquals(syslogDTO.getAddress().getType(), entity.getAddress().getType());
        assertEquals(syslogDTO.getAddress().getValue(), entity.getAddress().getValue());

        assertEquals(syslogDTO.getMessage().getMimeType(), entity.getMessage().getMimeType().getValue());
        assertEquals(syslogDTO.getMessage().getFileName(), entity.getMessage().getFileName());
        assertEquals(syslogDTO.getId(), entity.getMessage().getId().longValue());
        assertEquals(syslogDTO.getMessage().getName(),entity.getMessage().getName());
        assertEquals(syslogDTO.getMessage().loadAsString(),entity.getMessage().getResource());
        assertEquals(syslogDTO.getMessage().getResource().length(),entity.getMessage().getLength());
    }
}