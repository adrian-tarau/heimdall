package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

@Provider
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
        Simulation.Builder builder = new Simulation.Builder(Hashing.hash(resource.getPath()));
        builder.resource(resource).type(Simulation.Type.K6);
        builder.tag("k6").name(resource.getName()).description(resource.getDescription());
        return builder.build();
    }
}
