package net.microfalx.heimdall.rest.api;

/**
 * An exception raised during the execution of a simulation.
 */
public class SimulationExecutionException extends SimulationException {

    public SimulationExecutionException(String message) {
        super(message);
    }

    public SimulationExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
