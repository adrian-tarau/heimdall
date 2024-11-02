package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.bootstrap.metrics.Vector;
import net.microfalx.heimdall.rest.api.Metrics;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.resource.Resource;

public class TestSimulatorExecutor extends AbstractSimulationExecutor {

    public TestSimulatorExecutor(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected AbstractSimulator createSimulator() {
        return new TestSimulator();
    }

    @Override
    protected Output parseOutput(SimulationContext context, Resource resource) {
        return new SimulationOutput(context.getEnvironment(), getSimulation())
                .setDataReceived(Vector.create(Metrics.DATA_RECEIVED, Value.create(12)));
    }
}
