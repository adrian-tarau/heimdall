package net.microfalx.heimdall.protocol.snmp.mib;

/**
 * An exception for a MIB error.
 */
public class MibException extends RuntimeException {

    public MibException(String message) {
        super(message);
    }

    public MibException(String message, Throwable cause) {
        super(message, cause);
    }
}
