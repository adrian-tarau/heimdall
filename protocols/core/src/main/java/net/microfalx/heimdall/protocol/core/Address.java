package net.microfalx.heimdall.protocol.core;

/**
 * Represents an address (email, host, etc).
 */
public interface Address {

    /**
     * Creates an address with a name and a value.
     *
     * @param name  the name
     * @param value the value
     * @return a non-null instance
     */
    static Address create(String name, String value) {
        return new DefaultAddress(name, value);
    }

    /**
     * Creates an address with only a value.
     *
     * @param value the value
     * @return a non-null instance
     */
    static Address create(String value) {
        return new DefaultAddress(value, value);
    }

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
}
