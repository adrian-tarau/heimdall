package net.microfalx.heimdall.rest.api;

import net.microfalx.heimdall.infrastructure.api.Environment;

/**
 * A context in which the simulation runs.
 */
public interface SimulationContext {

    /**
     * Returns the environment targeted by a simulation.
     *
     * @return a non-null instance
     */
    Environment getEnvironment();
}
