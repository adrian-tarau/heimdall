package net.microfalx.heimdall.protocol.gelf.simulator;

import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.simulator.DsvProtocolDataSet;
import net.microfalx.heimdall.protocol.core.simulator.DsvProtocolDataSetFactory;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;

/**
 * A DSV based dataset for GELF (Graylog Extended Log Format) events.
 */
public abstract class GelfDataSet extends DsvProtocolDataSet {

    public GelfDataSet(Resource resource) {
        super(resource);
    }

    /**
     * Invoked during simulator to update a GELF event based on past log data.
     *
     * @param event the event
     */
    protected abstract void update(GelfEvent event, Address sourceAddress, Address targetAddress);

    /**
     * Converts the severity string to a {@link Severity} enum.
     *
     * @param severity the severity string
     * @return a non-null enum
     */
    protected final Severity getSeverity(String severity) {
        severity = StringUtils.toLowerCase(severity);
        return switch (severity) {
            case "info", "informational" -> Severity.INFORMATIONAL;
            case "warn", "warning" -> Severity.WARNING;
            case "error" -> Severity.ERROR;
            case "debug", "notice" -> Severity.DEBUG;
            case "emergency" -> Severity.ALERT;
            case "alert" -> Severity.ALERT;
            case "critical" -> Severity.CRITICAL;
            default -> Severity.INFORMATIONAL;
        };
    }


    public static abstract class Factory extends DsvProtocolDataSetFactory {

        public Factory(Resource resource) {
            super(resource);
        }

        @Override
        public abstract GelfDataSet createDataSet();
    }
}
