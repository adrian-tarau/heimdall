package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.Simulator;

public class K6SimulatorProvider implements Simulator.Provider {

    @Override
    public String getName() {
        return "Grafana K6";
    }

    @Override
    public boolean supports(Simulation simulation) {
        return simulation.getType() == Simulation.Type.K6;
    }

    @Override
    public Simulator create(Simulation simulation) {
        return new K6Simulator(simulation);
    }
}
