package net.microfalx.heimdall.rest.jmeter;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.archive.ArchiveUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;
import static net.microfalx.resource.ResourceUtils.toFile;
import static org.apache.commons.io.FileUtils.deleteDirectory;

@Slf4j
public class JmeterSimulator extends AbstractSimulator {

    private Resource htmlReport;
    private Resource jmeterLog;

    private boolean cleanupReports;

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
        arguments.add(toFile(htmlReport).getAbsolutePath());
        updateVariables(arguments, context);
        jmeterLog = getSessionWorkspace().resolve("jmeter.log");
        setLog(jmeterLog);
    }

    private void updateVariables(List<String> arguments, SimulationContext context) {
        for (Attribute attribute : context.getAttributes()) {
            String name = StringUtils.toIdentifier(attribute.getName()).toUpperCase();
            String value = ObjectUtils.toString(attribute.getValue());
            arguments.add("-J" + name + "=" + value);
        }
    }

    @Override
    protected void completion() throws IOException {
        super.completion();
        packageReport();
        if (htmlReport.exists()) setReport(htmlReport);
    }

    private void packageReport() {
        cleanupReportDirectory();
        this.htmlReport = zipReportDirectory();
    }

    private Resource zipReportDirectory() {
        Resource reportZip = getSessionWorkspace().resolve("report.zip");
        try {
            ArchiveUtils.archive(this.htmlReport, reportZip);
        } catch (Exception e) {
            LOGGER.error("Failed to archive report directory, root cause: '{}'", getRootCauseDescription(e));
        }
        try {
            deleteDirectory(toFile(htmlReport));
        } catch (IOException e) {
            LOGGER.error("Failed to remove report directory, root cause: '{}'", getRootCauseDescription(e));
        }
        return reportZip;
    }

    private void cleanupReportDirectory() {
        if (!cleanupReports) return;
        File directory = toFile(htmlReport);
        try {
            deleteDirectory(new File(directory, "sbadmin2-1.0.7"));
        } catch (IOException e) {
            LOGGER.warn("Failed to cleanup report directory", e);
        }
    }

    @Override
    protected Collection<Output> parseOutput(SimulationContext context, Resource resource) throws IOException {
        return new JmeterOutputParser(this, getSimulation(), context, resource).parse();
    }
}
