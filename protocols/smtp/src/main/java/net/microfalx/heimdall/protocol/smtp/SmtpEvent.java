package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.AbstractEvent;

/**
 * The email message.
 */
public class SmtpEvent extends AbstractEvent {

    public SmtpEvent() {
        super(Type.SMTP);
    }

    public SmtpEvent(String id) {
        super(Type.SMTP, id);
    }
}
