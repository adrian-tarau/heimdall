package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulator;

import java.io.File;
import java.util.List;

public class K6Simulator extends AbstractSimulator {

    @Override
    protected Options resolveOptions() {
        return new Options("k6").setName("Grafana K6").setVersion("0.54.0")
                .setLinuxPackage("k6-v${VERSION}-linux-amd64.tar.gz").setWindowsPackage("k6-v${VERSION}-windows-amd64.zip")
                .setMinimumFileCount(1).setLinuxExecutable("k6").setWindowsExecutable("k6.exe");
    }

    @Override
    protected void update(List<String> arguments, File input, File output, SimulationContext context) {
        arguments.add("run");
        arguments.add("--out");
        arguments.add("csv=" + output.getName());
        arguments.add(input.getName());
    }
}
