import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.Hashing;
import net.microfalx.resource.Resource;

public class GatlingSimulationProvider implements Simulation.Provider {

    @Override
    public boolean supports(Resource resource) {
        return "java".equalsIgnoreCase(resource.getFileExtension());
    }

    @Override
    public Simulation create(Resource resource) {
        Simulation.Builder builder = new Simulation.Builder(Hashing.hash(resource.getPath()));
        builder.resource(resource).type(Simulation.Type.GATLING);
        builder.tag("gatling").name(resource.getName()).description(resource.getDescription());
        return builder.build();
    }

    @Override
    public String getName() {
        return "Gatling";
    }
}
