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
     * Returns the simulation.
     *
     * @return a non-null instance
     */
    Simulation getSimulation();

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
     * Returns the error message (a short description) for the reason why the simulation failed.
     * @return the error message, null if not available
     */
    String getErrorMessage();

    /**
     * Returns the output of one scenario.
     *
     * @return a non-null instance
     */
    Collection<Output> getOutputs();

    /**
     * Returns the report (the content) produced by the simulator.
     * <p>
     * At least a text report will be produced by most simulators. An HTML report can be available with some simulators.
     * If the simulator can produce an HTML and text report, the HTML report will be returned.
     * <p>
     * Some simulators would generate multiple reports. In this case, the resource will contain an archive that will contain
     * all the reports. It is expected that the archive has a file in the root called <code>index.html</code> or
     * <code>report.txt</code> with the entry point of the reports.
     *
     * @return a non-ull instance
     */
    Resource getReport();

    /**
     * Returns a resource containing the log of the simulation.
     *
     * @return a non-null instance
     */
    Resource getLogs();

    /**
     * Returns a resource containing the data produced by the simulation.
     *
     * @return a non-null instance
     */
    Resource getData();
}
