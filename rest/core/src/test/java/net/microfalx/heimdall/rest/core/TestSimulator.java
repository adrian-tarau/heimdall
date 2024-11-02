package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.rest.api.SimulationContext;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TestSimulator extends AbstractSimulator {

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
}
