import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class GatlingSimulator extends AbstractSimulator {

    public GatlingSimulator(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected Options resolveOptions() {
        return new Options("gatling").setName("Gatling").setVersion("4.12.2")
                .setPackage("gatling-${VERSION}.tgz")
                .setWindowsExecutable("bin/gatling.bat").setLinuxExecutable("bin/gatling.sh");
    }

    @Override
    protected Collection<Output> parseOutput(SimulationContext context, Resource resource) throws IOException {
        return new GatlingOutParser(this, getSimulation(), context, resource).parse();
    }

    @Override
    protected void update(List<String> arguments, File input, File output, SimulationContext context) {

    }
}
