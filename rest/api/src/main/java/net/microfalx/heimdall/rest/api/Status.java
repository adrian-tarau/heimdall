package net.microfalx.heimdall.rest.api;

/**
 * An enum for the status of a simulation
 */
public enum Status {

    /**
     * The simulation outcome is unknown.
     */
    UNKNOWN,

    /**
     * The simulation was successful.
     */
    SUCCESSFUL,

    /**
     * The simulation has failed
     */
    FAILED,

    /**
     * The simulation was canceled
     */
    CANCELED
}
