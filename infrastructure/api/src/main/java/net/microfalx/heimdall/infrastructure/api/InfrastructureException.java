package net.microfalx.heimdall.infrastructure.api;

/**
 * Base class for all infrastructure exceptions.
 */
public class InfrastructureException extends RuntimeException {

    public InfrastructureException(String message) {
        super(message);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
