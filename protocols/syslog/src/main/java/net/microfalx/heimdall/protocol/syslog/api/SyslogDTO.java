package net.microfalx.heimdall.protocol.syslog.api;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.Part;

@Getter
@Setter
public class SyslogDTO {
    private long id;
    private Address address;
    private Part message;
    private Severity severity;
    private Facility facility;
}
