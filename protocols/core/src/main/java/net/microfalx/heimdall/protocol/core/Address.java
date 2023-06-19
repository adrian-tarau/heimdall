package net.microfalx.heimdall.protocol.core;

/**
 * Represents an address (email, host, etc).
 * <p>
 * The name of an address represents the friendly name and it could be a person
 */
public interface Address {

    /**
     * Creates an address with a name and a value.
     *
     * @param type the address type
     * @param name  the name
     * @param value the value
     * @return a non-null instance
     */
    static Address create(Type type,String name, String value) {
        return new DefaultAddress(type, name, value);
    }

    /**
     * Creates an address with only a value.
     *
     * @param type the address type
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
