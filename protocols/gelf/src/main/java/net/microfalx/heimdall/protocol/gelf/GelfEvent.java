package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import net.microfalx.heimdall.protocol.core.AbstractEvent;
import net.microfalx.heimdall.protocol.core.Severity;

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

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
