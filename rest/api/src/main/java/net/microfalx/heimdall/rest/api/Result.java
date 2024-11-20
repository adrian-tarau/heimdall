package net.microfalx.heimdall.rest.api;

import net.microfalx.resource.Resource;

import java.util.Collection;

/**
 * Holds the result of a simulation.
 * <p>
 * The result of the simulation holds one {@link Output} for each scenario.
 */
public interface Result {

    /**
     * Returns the simulation.
     *
     * @return a non-null instance
     */
    Simulation getSimulation();

    /**
     * Returns the output of one scenario.
     *
     * @return a non-null instance
     */
    Collection<Output> getOutputs();

    /**
     * Returns the resource containing the report of the simulation.
     *
     * @return a non-null instance
     */
    Resource getReport();

    /**
     * Returns a resource containing the log of the simulation.
     *
     * @return a non-null instance
     */
    Resource getLogs();
}
