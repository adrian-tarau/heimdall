package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.Nameable;
import org.atteo.classindex.IndexSubclasses;

/**
 * A simulator which knows how to start, execute and complete a simulation and returns the simulation results (the output).
 */
public interface Simulator {

    /**
     * Returns the simulation.
     *
     * @return a non-null instance
     */
    Simulation getSimulation();

    /**
     * Executes the simulation and returns the results of the simulation.
     *
     * @param context the context in which the simulation runs
     * @return a non-null instance
     */
    Output execute(SimulationContext context);

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
