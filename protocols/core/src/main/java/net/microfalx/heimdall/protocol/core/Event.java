package net.microfalx.heimdall.protocol.core;

import java.util.Collection;

/**
 * Holds an event.
 */
public interface Event {

    /**
     * Returns a unique identifier associated with an event.
     *
     * @return a non-null instance
     */
    String getId();

    /**
     * Returns the name for the event.
     *
     * @return a non-null instance
     */
    String getName();

    /**
     * Returns the type of the event.
     *
     * @return a non-null instance
     */
    Type getType();

    /**
     * Returns the body of the event (the message).
     * <p>
     * If the event has multiple types of bodies, this method will return null.
     *
     * @return a non-null instance
     */
    Body getBody();

    /**
     * Returns a collection with parts associated with the event.
     *
     * @return a non-null instance
     */
    Collection<Part> getParts();

    /**
     * En enum for the type of event.
     */
    enum Type {

        /**
         * Simple Mail Transfer Protocol
         */
        SMTP,
        /**
         * Simple Network Management Protocol
         */
        SNMP,

        /**
         * Graylog Extended Logging Format
         */
        GELF,
        /**
         * Standard for Message Logging
         */
        SYSLOG
    }
}
