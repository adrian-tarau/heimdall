package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.protocol.core.AbstractEvent;
import net.microfalx.heimdall.protocol.core.Severity;

@Getter
@Setter
@ToString(callSuper = true)
public class GelfEvent extends AbstractEvent {

    private Facility facility = Facility.LOCAL1;
    private com.cloudbees.syslog.Severity gelfSeverity = com.cloudbees.syslog.Severity.INFORMATIONAL;
    private String version = "1.1";
    private Throwable throwable;
    private String application;
    private String process;
    private String logger;
    private String thread;

    public GelfEvent() {
        super(Type.GELF);
    }

    public com.cloudbees.syslog.Severity getGelfSeverity() {
        return gelfSeverity;
    }

    public GelfEvent setGelfSeverity(com.cloudbees.syslog.Severity severity) {
        this.gelfSeverity = severity;
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
