package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;

import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * Holds an event received over the wire.
 */
public interface Event extends Attributes<Attribute> {

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
     * Returns the severity of the event.
     *
     * @return a non-null instance
     */
    Severity getSeverity();

    /**
     * Returns the source address of the event.
     *
     * @return a non-null instance
     */
    Address getSource();

    /**
     * Returns one or more target addresses for an event.
     *
     * @return a non-null instance
     */
    Collection<Address> getTargets();

    /**
     * Returns the time when the event was recevied.
     *
     * @return a non-null instance, null if not available
     */
    ZonedDateTime getReceivedAt();

    /**
     * Returns the time when the event was creates.
     *
     * @return a non-null instance, null if not available
     */
    ZonedDateTime getCreatedAt();

    /**
     * Returns the time when the event was sent.
     *
     * @return a non-null instance, null if not available
     */
    ZonedDateTime getSentAt();

    /**
     * Returns the body of the event (the message).
     * <p>
     * If the event has multiple types of bodies, this method will return null.
     *
     * @return a non-null instance
     */
    Body getBody();

    /**
     * Returns the body as string.
     *
     * @return the body, null if there is no body
     */
    String getBodyAsString();

    /**
     * Returns whether the event has a body attached.
     *
     * @return {@code true} if it has a body, {@code false} otherwise
     */
    boolean hasBody();

    /**
     * Returns a collection with parts associated with the event.
     *
     * @return a non-null instance
     */
    Collection<Part> getParts();

    /**
     * Returns whether the event has at least one attachment.
     *
     * @return {@code true} if it has at least one attachment, {@code false} otherwise
     */
    boolean hasAttachments();

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
