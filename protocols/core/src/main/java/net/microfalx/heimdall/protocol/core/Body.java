package net.microfalx.heimdall.protocol.core;

/**
 * Holds the body of the event.
 */
public class Body extends AbstractPart {

    public Body(AbstractEvent event) {
        super(event, Type.BODY);
    }
}
