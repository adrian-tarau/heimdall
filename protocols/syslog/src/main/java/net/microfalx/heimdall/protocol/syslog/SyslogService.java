package net.microfalx.heimdall.protocol.syslog;

import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.jpa.SyslogEvent;
import net.microfalx.heimdall.protocol.jpa.SyslogEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SyslogService extends ProtocolService<SyslogMessage> {

    @Autowired
    private SyslogEventRepository syslogEventRepository;

    /**
     * Handles a Syslog message received by the server.
     *
     * @param message the message
     */
    public void handle(SyslogMessage message) {
        SyslogEvent syslogEvent = new SyslogEvent();
        syslogEvent.setFacility(message.getFacility().numericalCode());
        syslogEvent.setSeverity(message.getSyslogSeverity().numericalCode());
        syslogEvent.setAddress(lookupAddress(message.getSource()));
        syslogEvent.setReceivedAt(message.getReceivedAt().toLocalDateTime());
        syslogEvent.setSentAt(message.getSentAt().toLocalDateTime());
        syslogEvent.setMessage(persistPart(message.getBody()));
        syslogEventRepository.save(syslogEvent);
    }

}
