package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.SimulationOutput;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class JmeterOutputParser {

    private final SimulationContext simulationContext;
    private final Simulation simulation;
    private final Resource resource;

    JmeterOutputParser(SimulationContext simulationContext, Simulation simulation, Resource resource) {
        requireNonNull(simulationContext);
        requireNonNull(simulation);
        requireNonNull(resource);
        this.simulationContext = simulationContext;
        this.simulation = simulation;
        this.resource = resource;
    }

    Output parse() {
        return new SimulationOutput(simulationContext.getEnvironment(), simulation);
    }

}
