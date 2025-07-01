package net.microfalx.heimdall.protocol.gelf.simulator;

import com.cloudbees.syslog.Severity;
import net.microfalx.bootstrap.dsv.DsvRecord;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.simulator.DsvProtocolDataSet;
import net.microfalx.heimdall.protocol.core.simulator.DsvProtocolDataSetFactory;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A DSV based dataset for GELF (Graylog Extended Log Format) events.
 */
public abstract class GelfDataSet extends DsvProtocolDataSet {

    protected static final String CORRELATION_ID = "correlationId";

    public GelfDataSet(Resource resource) {
        super(resource);
    }

    /**
     * Invoked during simulator to update a GELF event based on past log data.
     *
     * @param event the event
     */
    public final void update(GelfEvent event, Address sourceAddress, Address targetAddress) {
        DsvRecord record = iterator().next();
        updateCommonFields(event, sourceAddress, targetAddress);
        updateCommonFieldsFromRecord(event, record);
        update(event, sourceAddress, targetAddress, record);
    }

    /**
     * Invoked with the next available DSV record to update the GELF event.
     *
     * @param event         the event
     * @param sourceAddress the source address
     * @param targetAddress the target address
     * @param record        the DSV record
     */
    protected void update(GelfEvent event, Address sourceAddress, Address targetAddress, DsvRecord record) {
        // subclasses can override this method to add more fields
    }

    /**
     * Converts the severity string to a {@link Severity} enum.
     *
     * @param severity the severity string
     * @return a non-null enum
     */
    protected final Severity getSeverity(String severity) {
        severity = StringUtils.toLowerCase(severity);
        return switch (severity) {
            case "warn", "warning" -> Severity.WARNING;
            case "error" -> Severity.ERROR;
            case "debug", "notice" -> Severity.DEBUG;
            case "emergency" -> Severity.ALERT;
            case "alert", "critical" -> Severity.ALERT;
            default -> Severity.INFORMATIONAL;
        };
    }

    /**
     * Generates a correlational ID based on the event ID and event template.
     *
     * @param eventId       the event ID
     * @param eventTemplate the event template
     * @return a non-null instance
     */
    protected final String getCorrelationId(String eventId, String eventTemplate) {
        if (eventId == null && eventTemplate == null) return null;
        Hashing hashing = Hashing.create();
        hashing.update(eventId);
        hashing.update(eventTemplate);
        return hashing.asString();
    }

    private void updateCommonFields(GelfEvent event, Address sourceAddress, Address targetAddress) {
        event.setApplication(getName());
        event.setSource(sourceAddress);
        event.addTarget(targetAddress);
    }

    private void updateCommonFieldsFromRecord(GelfEvent event, DsvRecord record) {
        event.add(CORRELATION_ID, getCorrelationId(record.get("EventId"), record.get("EventTemplate")));
        String level = record.get("level");
        if (isNotEmpty(level)) event.setGelfSeverity(getSeverity(level));
        String component = record.get("component");
        if (isNotEmpty(component)) event.setLogger(component);
        String content = record.get("content");
        if (isNotEmpty(content)) event.setBody(Body.create(content));
    }


    public static abstract class Factory extends DsvProtocolDataSetFactory {

        public Factory(Resource resource) {
            super(resource);
        }

        @Override
        public abstract GelfDataSet createDataSet();
    }
}
