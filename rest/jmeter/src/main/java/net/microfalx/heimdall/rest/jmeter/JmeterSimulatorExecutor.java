package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulationExecutor;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.resource.Resource;

public class JmeterSimulatorExecutor extends AbstractSimulationExecutor {

    public JmeterSimulatorExecutor(Simulation simulation) {
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
