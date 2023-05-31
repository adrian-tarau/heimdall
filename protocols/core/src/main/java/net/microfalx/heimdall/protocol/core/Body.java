package net.microfalx.heimdall.protocol.core;

import net.microfalx.resource.MemoryResource;

/**
 * Holds the body of the event.
 */
public class Body extends AbstractPart {

    /**
     * Creates a body from a text.
     *
     * @param event the event
     * @param text  the text
     * @param <E>   the event type
     * @return a non-null instance
     */
    public static <E extends AbstractEvent> Body create(E event, String text) {
        Body body = new Body(event);
        body.setResource(MemoryResource.create(text));
        return body;
    }

    public Body(AbstractEvent event) {
        super(event, Type.BODY);
    }
}
