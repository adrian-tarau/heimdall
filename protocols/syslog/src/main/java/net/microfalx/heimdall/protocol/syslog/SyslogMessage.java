package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.AbstractEvent;

public class SyslogMessage extends AbstractEvent {

    private Facility facility;
    private Severity severity;

    public SyslogMessage() {
        super(Type.SYSLOG);
    }

    public Facility getFacility() {
        return facility;
    }

    public SyslogMessage setFacility(Facility facility) {
        this.facility = facility;
        return this;
    }

    public Severity getSeverity() {
        return severity;
    }

    public SyslogMessage setSeverity(Severity severity) {
        this.severity = severity;
        return this;
    }
}
