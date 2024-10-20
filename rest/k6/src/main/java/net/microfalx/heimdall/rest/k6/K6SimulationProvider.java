package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.resource.Resource;

public class K6SimulationProvider implements Simulation.Provider{

    @Override
    public String getName() {
        return "Grafana K6";
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
