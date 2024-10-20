package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationExecutor;

public class K6SimulationExecutorProvider implements SimulationExecutor.Provider {

    @Override
    public String getName() {
        return "Grafana K6";
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
