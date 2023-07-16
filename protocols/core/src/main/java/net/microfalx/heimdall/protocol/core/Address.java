package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.ExceptionUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents an address (email, host, etc).
 * <p>
 * The name of an address represents the friendly name and it could be a person
 */
public interface Address {

    /**
     * Creates an address from an email address.
     *
     * @param value the email
     * @return a non-null instance
     */
    static Address email(String value) {
        return email(value, null);
    }

    /**
     * Creates an address from an email address.
     *
     * @param value the email
     * @param name  the name of the sender
     * @return a non-null instance
     */
    static Address email(String value, String name) {
        return create(Type.HOSTNAME, value, name);
    }

    /**
     * Creates an address from a hostname/IP address.
     *
     * @param value the hostname/IP
     * @return a non-null instance
     */
    static Address host(String value) {
        return host(value, null);
    }

    /**
     * Creates an address from a {@link InetAddress}.
     *
     * @param value the hostname/IP
     * @return a non-null instance
     */
    static Address host(InetAddress value) {
        return host(value.getHostAddress(), value.getHostName());
    }

    /**
     * Creates an address from a hostname/IP address.
     *
     * @param value the hostname/IP
     * @param name  the hostname
     * @return a non-null instance
     */
    static Address host(String value, String name) {
        return create(Type.HOSTNAME, value, name);
    }

    /**
     * Creates an address from the local system address.
     *
     * @return a non-null instance
     */
    static Address local() {
        try {
            return host(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    /**
     * Creates an address with a name and a value.
     *
     * @param type  the address type
     * @param name  the name
     * @param value the value
     * @return a non-null instance
     */
    static Address create(Type type, String name, String value) {
        return new DefaultAddress(type, name, value);
    }

    /**
     * Creates an address with only a value.
     *
     * @param type  the address type
     * @param value the value
     * @return a non-null instance
     */
    static Address create(Type type, String value) {
        return new DefaultAddress(type, value, value);
    }

    /**
     * Returns the type of address.
     *
     * @return a non null instance
     */
    Type getType();

    /**
     * Returns the name for the event.
     *
     * @return a non-null instance
     */
    String getName();

    /**
     * Returns the value of the address.
     *
     * @return a non-null instance
     */
    String getValue();

    /**
     * Returns the address as a string suitable for display.
     * @return a non-null instance
     */
    String toDisplay();

    /**
     * An enum for the address type
     */
    enum Type {

        /**
         * An email address
         */
        EMAIL,

        /**
         * A hostname or IP
         */
        HOSTNAME,

        /**
         * A catch all type
         */
        OTHER
    }
}
