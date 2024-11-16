package net.microfalx.heimdall.rest.api;

/**
 * Base exception for all simulation exceptions.
 */
public class SimulationException extends RestException {

    public SimulationException(String message) {
        super(message);
    }

    public SimulationException(String message, Throwable cause) {
        super(message, cause);
    }
}
