package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulationExecutor;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.resource.Resource;

public class K6SimulatorExecutor extends AbstractSimulationExecutor {

    public K6SimulatorExecutor(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected AbstractSimulator createSimulator() {
        return null;
    }

    @Override
    protected Output parseOutput(SimulationContext context, Resource resource) {
        return null;
    }
}
