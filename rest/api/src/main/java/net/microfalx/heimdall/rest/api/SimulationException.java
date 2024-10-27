package net.microfalx.heimdall.rest.api;

/**
 * Base exception for all simulation exception.
 */
public class SimulationException extends RuntimeException {

    public SimulationException(String message) {
        super(message);
    }

    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
