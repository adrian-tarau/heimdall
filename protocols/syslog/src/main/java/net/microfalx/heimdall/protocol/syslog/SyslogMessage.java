package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import net.microfalx.heimdall.protocol.core.AbstractEvent;
import net.microfalx.heimdall.protocol.core.Severity;

public class SyslogMessage extends AbstractEvent {

    private Facility facility;
    private com.cloudbees.syslog.Severity severity;

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

    public com.cloudbees.syslog.Severity getSyslogSeverity() {
        return severity;
    }

    public SyslogMessage setSyslogSeverity(com.cloudbees.syslog.Severity severity) {
        this.severity = severity;
        switch (severity) {
            case DEBUG -> setSeverity(Severity.DEBUG);
            case INFORMATIONAL -> setSeverity(Severity.INFO);
            case WARNING -> setSeverity(Severity.WARN);
            case ERROR, EMERGENCY, CRITICAL -> setSeverity(Severity.ERROR);
            default -> setSeverity(Severity.TRACE);
        }
        return this;
    }
}
