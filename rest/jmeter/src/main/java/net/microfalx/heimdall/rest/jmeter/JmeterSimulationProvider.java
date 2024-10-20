package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.resource.Resource;

public class JmeterSimulationProvider implements Simulation.Provider {

    @Override
    public String getName() {
        return "Apache JMeter";
    }

    @Override
    public boolean supports(Resource resource) {
        return false;
    }

    @Override
    public Simulation create(Resource resource) {
        return null;
    }
}
