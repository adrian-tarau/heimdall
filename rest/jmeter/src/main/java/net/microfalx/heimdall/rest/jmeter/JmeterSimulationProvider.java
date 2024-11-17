package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.StringUtils.capitalizeWords;

@Provider
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
        Simulation.Builder builder = new Simulation.Builder(Hashing.hash(resource.getPath())).resource(resource).type(Simulation.Type.JMETER);
        builder.tag("jmeter").name(capitalizeWords(resource.getName()))
                .description(resource.getDescription());
        return builder.build();
    }
}
