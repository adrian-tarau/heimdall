package net.microfalx.heimdall.protocol.core;

import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds the body of the event.
 */
public class Body extends AbstractPart {

    /**
     * Creates a body from a plain text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static Body plain(String text) {
        return (Body) create(text).setMimeType(MimeType.TEXT_PLAIN);
    }

    /**
     * Creates a body from an HTML text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static Body html(String text) {
        return (Body) create(text).setMimeType(MimeType.TEXT_HTML);
    }

    /**
     * Creates a body from a text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static Body create(String text) {
        Body body = new Body();
        body.setResource(MemoryResource.create(text).withMimeType(MimeType.TEXT_PLAIN));
        body.setName(text);
        return body;
    }

    /**
     * Creates a body from a resource.
     *
     * @param resource the resource
     * @return a non-null instance
     */
    public static Body create(Resource resource) {
        Body body = new Body();
        body.setResource(resource);
        return body;
    }

    /**
     * Creates a body from a resource.
     *
     * @param resource the resource
     * @return a non-null instance
     */
    public static Body create(Resource resource, Event event) {
        requireNonNull(event);
        Body body = new Body();
        body.setResource(resource);
        body.setEvent((AbstractEvent) event);
        return body;
    }

    public Body() {
        super(Type.BODY);
    }

}
