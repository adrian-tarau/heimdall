package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.Simulator;

public class JmeterSimulatorProvider implements Simulator.Provider {

    @Override
    public String getName() {
        return "Apache JMeter";
    }

    @Override
    public boolean supports(Simulation simulation) {
        return simulation.getType() == Simulation.Type.JMETER;
    }

    @Override
    public Simulator create(Simulation simulation) {
        return new JmeterSimulator(simulation);
    }

}
