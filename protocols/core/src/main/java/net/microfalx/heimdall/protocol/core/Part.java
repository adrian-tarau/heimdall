package net.microfalx.heimdall.protocol.core;

import net.microfalx.resource.Resource;

/**
 * Holds a part of the event.
 */
public interface Part {

    /**
     * Returns a unique identifier for a part.
     *
     * @return a non-null instance
     */
    String getId();

    /**
     * Return the event.
     *
     * @return a non-null instance
     */
    Event getEvent();

    /**
     * Returns the event part.
     *
     * @return a non-null instance
     */
    Type getType();

    /**
     * Returns the content type associated with the part.
     *
     * @return a non-null instance
     */
    String getContentType();

    /**
     * Returns the file name associated with the part.
     *
     * @return the file name, null if none is available
     */
    String getFileName();

    /**
     * Returns the resource to access the content.
     *
     * @return a non-null instance
     */
    Resource getResource();

    /**
     * A type for a part
     */
    enum Type {

        /**
         * The part is stored with the event
         */
        BODY,

        /**
         * The part is an attachment which follows the event
         */
        ATTACHMENT
    }
}
