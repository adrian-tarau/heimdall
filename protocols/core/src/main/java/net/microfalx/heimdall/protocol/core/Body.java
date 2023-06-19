package net.microfalx.heimdall.protocol.core;

import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;

/**
 * Holds the body of the event.
 */
public class Body extends AbstractPart {

    /**
     * Creates a body from a text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static Body create(String text) {
        Body body = new Body();
        body.setResource(MemoryResource.create(text));
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

    public Body() {
        super(Type.BODY);
    }
}
