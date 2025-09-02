package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.protocol.core.AbstractEvent;
import net.microfalx.heimdall.protocol.core.Severity;

@Getter
@Setter
@ToString
public class SyslogMessage extends AbstractEvent {

    private Facility facility;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private com.cloudbees.syslog.Severity severity;

    public SyslogMessage() {
        super(Type.SYSLOG);
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
