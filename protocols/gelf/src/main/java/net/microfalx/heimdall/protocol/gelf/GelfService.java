package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.protocol.core.ProtocolService;
import org.springframework.stereotype.Service;

@Service
public class GelfService extends ProtocolService<GelfMessage> {

    /**
     * Handle one GELF message.
     *
     * @param message the GELF message
     */
    public void handle(GelfMessage message) {

    }
}
