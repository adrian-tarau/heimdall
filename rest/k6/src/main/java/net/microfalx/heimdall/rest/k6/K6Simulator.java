package net.microfalx.heimdall.rest.k6;

import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.lang.TimeUtils;
import net.microfalx.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static net.microfalx.lang.FileUtils.removeFileExtension;
import static net.microfalx.lang.JvmUtils.getNextAvailablePort;
import static net.microfalx.lang.UriUtils.parseUrl;

public class K6Simulator extends AbstractSimulator {

    private Resource htmlReport;
    private int port;

    public K6Simulator(Simulation simulation) {
        super(simulation);
    }

    @Override
    public Optional<URL> getDashboardUrl() {
        return Optional.of(parseUrl("http://localhost:" + port));
    }

    @Override
    protected Options resolveOptions() {
        return new Options("k6").setName("Grafana K6").setVersion("0.54.0")
                .setLinuxPackage("k6-v${VERSION}-linux-amd64.tar.gz").setWindowsPackage("k6-v${VERSION}-windows-amd64.zip")
                .setMinimumFileCount(1).setLinuxExecutable("k6").setWindowsExecutable("k6.exe");
    }

    @Override
    protected void update(List<String> arguments, File input, File output, SimulationContext context) {
        arguments.add("run");
        updateCoreArguments(arguments, input, output, context);
        updateAdditionalArguments(arguments, context);
    }

    @Override
    protected void update(ProcessBuilder process, SimulationContext context) {
        super.update(process, context);
        process.environment().put("K6_WEB_DASHBOARD", "true");
        String port = process.environment().get("K6_WEB_DASHBOARD_PORT");
        if (port == null) port = Integer.toString(getNextAvailablePort(40_000));
        this.port = Integer.parseInt(port);
        process.environment().put("K6_WEB_DASHBOARD_PORT", port);
        String simulationFileName = removeFileExtension(getSimulation().getResource().getFileName());
        process.environment().put("K6_WEB_DASHBOARD_EXPORT", simulationFileName + ".html");
        process.environment().put("K6_WEB_DASHBOARD_PERIOD", "5s");
        process.environment().put("K6_CSV_TIME_FORMAT", "unix_milli");
        htmlReport = getSessionWorkspace().resolve(simulationFileName + ".html");
    }

    @Override
    protected void completion() throws IOException {
        super.completion();
        setReport(htmlReport);
    }

    @Override
    protected Collection<Output> parseOutput(SimulationContext context, Resource resource) throws IOException {
        return new K6OutputParser(this, getSimulation(), context, resource).parse();
    }

    private void updateCoreArguments(List<String> arguments, File input, File output, SimulationContext context) {
        arguments.add("--out");
        arguments.add("csv=" + output.getName());
        arguments.add("--no-usage-report");
        arguments.add("--insecure-skip-tls-verify");
        arguments.add(input.getName());
    }

    private void updateAdditionalArguments(List<String> arguments, SimulationContext context) {
        Attributes<?> attributes = context.getAttributes();
        Attribute vus = attributes.get("vus");
        if (!vus.isEmpty()) {
            arguments.add("--vus");
            arguments.add(vus.asString());
        }
        Attribute iterations = attributes.get("iterations");
        if (!iterations.isEmpty()) {
            arguments.add("--iterations");
            arguments.add(iterations.asString());
        }
        Attribute duration = attributes.get("duration");
        if (!duration.isEmpty()) {
            arguments.add("--duration");
            Duration value = (Duration) duration.getValue();
            arguments.add(TimeUtils.toString(value));
        }
    }
}
