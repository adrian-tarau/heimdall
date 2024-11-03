package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
        arguments.add("--out");
        arguments.add("csv=" + output.getName());
        arguments.add(input.getName());
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
        htmlReport = getSessionWorkspace().resolve(simulationFileName + ".html");
    }

    @Override
    protected void completion() throws IOException {
        super.completion();
        if (htmlReport.exists()) addReport(htmlReport);
    }

    @Override
    protected Collection<Output> parseOutput(SimulationContext context, Resource resource) throws IOException {
        return new K6OutputParser(context, getSimulation(), resource).parse();
    }
}
