package net.microfalx.heimdall.protocol.gelf.api;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import io.azam.ulidj.ULID;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.Part;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class GelfDTO {
    private long id;
    private String name;
    private Address source;
    private Facility facility = Facility.LOCAL1;
    private com.cloudbees.syslog.Severity severity = Severity.INFORMATIONAL;
    private String version = "1.1";
    private Part shortMessage;
    private Part longMessage;
    private Part fields;
    private String application;
    private String process;
    private String logger;
    private String thread;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime sentAt;
}
