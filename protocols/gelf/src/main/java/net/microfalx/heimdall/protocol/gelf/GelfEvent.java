package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import net.microfalx.heimdall.protocol.core.AbstractEvent;
import net.microfalx.heimdall.protocol.core.Severity;

public class GelfEvent extends AbstractEvent {

    private Facility facility;
    private com.cloudbees.syslog.Severity gelfSeverity;
    private String version = "1.1";
    private Throwable throwable;

    public GelfEvent() {
        super(Type.GELF);
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
