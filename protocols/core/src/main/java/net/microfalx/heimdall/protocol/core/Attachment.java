package net.microfalx.heimdall.protocol.core;

import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;

/**
 * An email attachment.
 */
public class Attachment extends AbstractPart {

    /**
     * Creates a body from a text.
     *
     * @param text the text
     * @return a non-null instance
     */
    public static Attachment create(String text) {
        Attachment attachment = new Attachment();
        attachment.setResource(MemoryResource.create(text));
        return attachment;
    }

    /**
     * Creates a body from a resource.
     *
     * @param resource the resource
     * @return a non-null instance
     */
    public static Attachment create(Resource resource) {
        Attachment attachment = new Attachment();
        attachment.setResource(resource);
        return attachment;
    }

    public Attachment() {
        super(Type.ATTACHMENT);
    }
}
