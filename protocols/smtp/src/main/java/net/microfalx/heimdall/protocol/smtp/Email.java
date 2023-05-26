package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.AbstractEvent;

/**
 * The email message.
 */
public class Email extends AbstractEvent {

    public Email() {
        super(Type.SMTP);
    }

}
