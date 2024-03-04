package net.microfalx.heimdall.broker.core;

/**
 * Base class for all broker exceptions.
 */
public class BrokerException extends RuntimeException {

    public BrokerException(String message) {
        super(message);
    }

    public BrokerException(String message, Throwable cause) {
        super(message, cause);
    }
}
