package net.microfalx.heimdall.protocol.syslog.api;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import io.azam.ulidj.ULID;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.resource.Resource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

@Getter
@Setter
public class SyslogDTO{
    private long id;
    private String name;
    private Address source;
    private Facility facility;
    private com.cloudbees.syslog.Severity severity = Severity.INFORMATIONAL;
    private Part message;
    private LocalDateTime receivedAt;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime sentAt;
}
