package net.microfalx.heimdall.infrastructure.api;

/**
 * An exception raised when a component from the infrastructure cannot be found.
 */
public class InfrastructureNotFoundException extends InfrastructureException {

    public InfrastructureNotFoundException(String message) {
        super(message);
    }

    public InfrastructureNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
