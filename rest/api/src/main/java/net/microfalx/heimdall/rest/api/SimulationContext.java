package net.microfalx.heimdall.rest.api;

import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.heimdall.infrastructure.api.Environment;

import java.util.Collection;
import java.util.Optional;

/**
 * A context in which the simulation runs.
 */
public interface SimulationContext {

    /**
     * Returns whether the simulation was triggered manually.
     *
     * @return {@code true} if manual, {@code false} otherwise
     */
    boolean isManual();

    /**
     * Changes whether the simulation was triggered manually.
     *
     * @param manual {@code true} if manual, {@code false} otherwise
     * @return self
     */
    SimulationContext setManual(boolean manual);

    /**
     * Returns the optional user which triggered the context.
     *
     * @return a non-null instance
     */
    Optional<String> getUser();

    /**
     * Sets the user which triggered the simulation.
     *
     * @param user the user
     * @return self
     */
    SimulationContext setUser(String user);

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
