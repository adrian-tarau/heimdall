package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationExecutor;

public class JmeterSimulationExecutorProvider implements SimulationExecutor.Provider {

    @Override
    public String getName() {
        return "Apache JMeter";
    }

    @Override
    public boolean supports(Simulation simulation) {
        return false;
    }

    @Override
    public SimulationExecutor create(Simulation simulation) {
        return null;
    }
}
