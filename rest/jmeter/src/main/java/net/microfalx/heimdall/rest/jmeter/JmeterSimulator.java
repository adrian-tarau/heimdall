package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class JmeterSimulator extends AbstractSimulator {

    private Resource htmlReport;
    private Resource jmeterLog;

    public JmeterSimulator(Simulation simulation) {
        super(simulation);
    }

    @Override
    protected Options resolveOptions() {
        return new Options("jmeter").setName("Apache JMeter").setVersion("5.6.3")
                .setPackage("apache-jmeter-${VERSION}.tgz")
                .setWindowsExecutable("bin/jmeter.bat").setLinuxExecutable("bin/jmeter.sh");
    }

    @Override
    protected void update(List<String> arguments, File input, File output, SimulationContext context) {
        arguments.add("-n");
        arguments.add("-t");
        arguments.add(input.getName());
        arguments.add("-l");
        arguments.add(output.getName());
        htmlReport = getSessionWorkspace().resolve("html", Resource.Type.DIRECTORY);
        arguments.add("-e");
        arguments.add("-o");
        arguments.add(ResourceUtils.toFile(htmlReport).getAbsolutePath());
        jmeterLog = getSessionWorkspace().resolve("jmeter.log");
        setLog(jmeterLog);
    }

    @Override
    protected void completion() throws IOException {
        super.completion();
        if (htmlReport.exists()) setReport(htmlReport);
    }

    @Override
    protected Collection<Output> parseOutput(SimulationContext context, Resource resource) throws IOException {
        return new JmeterOutputParser(this, getSimulation(), context, resource).parse();
    }
}