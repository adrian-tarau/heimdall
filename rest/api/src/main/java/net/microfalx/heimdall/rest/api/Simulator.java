package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;
import org.atteo.classindex.IndexSubclasses;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A simulator which knows how to start, execute and complete a simulation and returns the simulation results (the output).
 */
public interface Simulator extends Identifiable<String>, Nameable {

    /**
     * Returns the simulation.
     *
     * @return a non-null instance
     */
    Simulation getSimulation();

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
     * Returns whether the simulator is actually running a simulation.
     *
     * @return {@code true} if running, {@code false} otherwise
     */
    boolean isRunning();

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
     * Executes the simulation and returns the results of the simulation.
     *
     * @param context the context in which the simulation runs
     * @return a non-null instance
     */
    Result execute(SimulationContext context);

    /**
     * Aborts a running simulation.
     */
    void abort();

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
     * Returns an optional URL to the live dashboard of the simulator.
     *
     * @return a non-null instance
     */
    Optional<URL> getDashboardUrl();

    @IndexSubclasses
    interface Provider extends Nameable {

        /**
         * Returns whether this provider can execute a given simulation\.
         *
         * @param simulation the simulation
         * @return {@code true} if a simulation can be supported, {@code false} otherwise
         */
        boolean supports(Simulation simulation);

        /**
         * Creates a simulator for a given simulation.
         *
         * @param simulation the simulation
         * @return a non-null instance
         */
        Simulator create(Simulation simulation);
    }
}
