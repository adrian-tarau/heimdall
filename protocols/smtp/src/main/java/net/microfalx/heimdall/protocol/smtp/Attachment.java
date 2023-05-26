package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.AbstractEvent;
import net.microfalx.heimdall.protocol.core.AbstractPart;

/**
 * An email attachment.
 */
public class Attachment extends AbstractPart {

    public Attachment(AbstractEvent event, Type type) {
        super(event, type);
    }
}
