package net.microfalx.heimdall.rest.api;

import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.heimdall.infrastructure.api.Environment;

import java.util.Collection;

/**
 * A context in which the simulation runs.
 */
public interface SimulationContext {

    /**
     * Returns the simulation to be executed.
     *
     * @return a non-null instance
     */
    Simulation getSimulation();

    /**
     * Returns the environment targeted by a simulation.
     *
     * @return a non-null instance
     */
    Environment getEnvironment();

    /**
     * Returns the parameters passed to the simulator.
     * <p>
     * The attributes will contain be based on attributes received from environment and any custom
     * attributes
     *
     * @return a non-null instance
     */
    Attributes<?> getAttributes();

    /**
     * Returns the library available for the simulation.
     *
     * @return a non-null instance
     */
    Collection<Library> getLibraries();
}
