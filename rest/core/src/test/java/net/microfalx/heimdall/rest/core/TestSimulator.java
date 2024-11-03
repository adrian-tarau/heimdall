package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.bootstrap.metrics.Vector;
import net.microfalx.heimdall.rest.api.Metrics;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestSimulator extends AbstractSimulator {

    public TestSimulator(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected Options resolveOptions() {
        return new Options("sim").setName("Test Simulator").setVersion("1.0")
                .setPackage("sim-${VERSION}.zip").setWindowsExecutable("bin/sim.bat").setLinuxExecutable("bin/sim.sh")
                .addFiles("bin/sim.bat", "config/settings.properties", "lib/slf4j-api-2.0.16.jar");
    }

    @Override
    protected void update(List<String> arguments, File input, File output, SimulationContext context) {
        arguments.addAll(Arrays.asList("-i", input.getName(), "-o", output.getName()));
    }

    @Override
    protected Output parseOutput(SimulationContext context, Resource resource) throws IOException {
        return new SimulationOutput(context.getEnvironment(), getSimulation())
                .setDataReceived(Vector.create(Metrics.DATA_RECEIVED, Value.create(12)));
    }
}
