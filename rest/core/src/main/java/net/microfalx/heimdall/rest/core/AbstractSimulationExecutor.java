package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.api.SimulationExecutor;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all executors.
 */
@Slf4j
public abstract class AbstractSimulationExecutor implements SimulationExecutor {

    private final Simulation simulation;

    public AbstractSimulationExecutor(Simulation simulation) {
        requireNonNull(simulation);
        this.simulation = simulation;
    }

    @Override
    public final Simulation getSimulation() {
        return simulation;
    }

    @Override
    public final Output execute(SimulationContext context) {
        LOGGER.debug("Execute simulation {}", simulation.getName());
        AbstractSimulator simulator = createSimulator();
        Resource resource = simulator.execute(getSimulation(), context);
        return parseOutput(context, resource);
    }

    /**
     * Creates an instance of the simulator.
     *
     * @return a non-null instance
     */
    protected abstract AbstractSimulator createSimulator();

    /**
     * Parses the output of the executor and returns a simulation output.
     *
     * @param context  the simulation context
     * @param resource the simulator output
     * @return the simulation output
     */
    protected abstract Output parseOutput(SimulationContext context, Resource resource);
}
