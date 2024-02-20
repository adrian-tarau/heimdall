package net.microfalx.heimdall.protocol.syslog;

import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEvent;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class SyslogService extends ProtocolService<SyslogMessage, SyslogEvent> {

    @Autowired
    private SyslogEventRepository syslogEventRepository;

    @Autowired
    private SyslogSimulator syslogSimulator;

    @Autowired
    private SyslogConfiguration syslogConfiguration;

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SYSLOG;
    }

    @Override
    protected String getControllerPath() {
        return "/protocol/syslog";
    }

    @Override
    protected void prepare(SyslogMessage message) {
        lookupAddress(message.getSource());
    }

    protected void persist(SyslogMessage message) {
        SyslogEvent syslogEvent = new SyslogEvent();
        syslogEvent.setFacility(message.getFacility().numericalCode());
        syslogEvent.setSeverity(message.getSyslogSeverity().numericalCode());
        syslogEvent.setAddress(lookupAddress(message.getSource()));
        syslogEvent.setCreatedAt(message.getCreatedAt().toLocalDateTime());
        syslogEvent.setReceivedAt(message.getReceivedAt().toLocalDateTime());
        syslogEvent.setSentAt(message.getSentAt().toLocalDateTime());
        syslogEvent.setMessage(persistPart(message.getBody()));
        syslogEventRepository.save(syslogEvent);
        updateReference(message, syslogEvent.getId());
    }

    /**
     * Returns the simulator.
     *
     * @return the simulator, null if not supported
     */
    @Override
    protected SyslogSimulator getSimulator() {
        return syslogSimulator;
    }
}
