package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulator;

import java.io.File;
import java.util.List;

public class JmeterSimulator extends AbstractSimulator {

    @Override
    protected Options resolveOptions() {
        return new Options("jmeter").setName("Apache JMeter").setVersion("5.6.3")
                .setPackage("apache-jmeter-${VERSION}.tgz")
                .setWindowsExecutable("bin/jmeter.bat").setLinuxExecutable("bin/jmeter.sh");
    }

    @Override
    protected void update(List<String> arguments, File input, File output, SimulationContext context) {

    }
}
