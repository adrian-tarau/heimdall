package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;

public class JmeterSimulationProvider implements Simulation.Provider {

    @Override
    public String getName() {
        return "Apache JMeter";
    }

    @Override
    public boolean supports(Resource resource) {
        return "jmx".equalsIgnoreCase(resource.getFileExtension());
    }

    @Override
    public Simulation create(Resource resource) {
        Simulation.Builder builder = new Simulation.Builder().resource(resource).
                type(Simulation.Type.JMETER);
        builder.name(StringUtils.toIdentifier(resource.getName()));
        builder.description(resource.getDescription());
        return builder.build();
    }
}
