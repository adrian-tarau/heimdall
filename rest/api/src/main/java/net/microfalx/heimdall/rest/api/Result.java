package net.microfalx.heimdall.rest.api;

import net.microfalx.resource.Resource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Holds the result of a simulation.
 * <p>
 * The result of the simulation holds one {@link Output} for each scenario.
 */
public interface Result {

    /**
     * Returns the time when the simulation was started.
     *
     * @return a non-null instance
     */
    LocalDateTime getStartTime();

    /**
     * Returns the time when the simulation has ended.
     *
     * @return a non-null instance
     */
    LocalDateTime getEndTime();

    /**
     * Returns the duration of the simulation.
     * <p>
     * If the simulation is not completed, it returns the duration of the simulation until now.
     *
     * @return a non-null instance
     */
    Duration getDuration();

    /**
     * Returns the status of the simulation.
     *
     * @return a non-null instance
     */
    Status getStatus();

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
