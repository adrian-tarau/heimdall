package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.StringUtils.capitalizeWords;

public class K6SimulationProvider implements Simulation.Provider {

    @Override
    public String getName() {
        return "Grafana K6";
    }

    @Override
    public boolean supports(Resource resource) {
        return "js".equalsIgnoreCase(resource.getFileExtension());
    }

    @Override
    public Simulation create(Resource resource) {
        Simulation.Builder builder = new Simulation.Builder().resource(resource).
                type(Simulation.Type.K6);
        builder.name(capitalizeWords(resource.getName()));
        builder.description(resource.getDescription());
        builder.tag("");
        return builder.build();
    }
}
