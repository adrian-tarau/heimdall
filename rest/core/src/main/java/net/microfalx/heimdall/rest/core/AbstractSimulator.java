package net.microfalx.heimdall.rest.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.lang.*;
import net.microfalx.resource.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.heimdall.rest.api.RestConstants.LOG_ATTR;
import static net.microfalx.heimdall.rest.api.RestConstants.REPORT_ATTR;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.FormatterUtils.formatBytes;
import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.lang.JvmUtils.isWindows;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.TextUtils.LINE_SEPARATOR;
import static net.microfalx.lang.TextUtils.getHeader;
import static net.microfalx.resource.ResourceUtils.toFile;

/**
 * Base class for the simulator.
 */
@Slf4j
public abstract class AbstractSimulator implements Simulator, Comparable<AbstractSimulator> {

    private final String id = UUID.randomUUID().toString();
    private final Simulation simulation;
    private Options options;
    private Resource installWorkspace;
    private Resource sessionWorkspace;
    private Resource input;
    private Resource systemOutput;
    private Resource systemError;
    private Resource output = Resource.NULL;

    private LocalDateTime startTime = LocalDateTime.now();
    private volatile LocalDateTime endTime;
    private volatile boolean running;
    private volatile Status status = Status.UNKNOWN;
    private volatile String errorMessage;
    private volatile Environment environment;
    private volatile Resource report = Resource.NULL;
    private volatile Resource log = Resource.NULL;
    private final StringBuilder logger = new StringBuilder(8000);
    private volatile Process process;

    private static final Set<String> INSTALLED = new CopyOnWriteArraySet<>();

    public AbstractSimulator(Simulation simulation) {
        requireNonNull(simulation);
        this.simulation = simulation;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getName() {
        return getOptions().getName();
    }

    @Override
    public Environment getEnvironment() {
        if (environment == null) {
            throw new IllegalStateException("The environment is available only for started simulations");
        }
        return environment;
    }

    @Override
    public final Simulation getSimulation() {
        return simulation;
    }

    @Override
    public final boolean isRunning() {
        return running;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public Resource getReport() {
        return report;
    }

    @Override
    public Resource getLogs() {
        StringBuilder finalLogs = null;
        try {
            finalLogs = new StringBuilder();
            finalLogs.append(getHeader("Execution Log")).append(LINE_SEPARATOR).append(logger).append(LINE_SEPARATOR);
            if (log.exists()) {
                finalLogs.append(getHeader("Simulator Log")).append(LINE_SEPARATOR).append(log.loadAsString());
            }
            String output = EMPTY_STRING;
            if (systemOutput.exists()) output = systemOutput.loadAsString();
            if (isNotEmpty(output)) {
                finalLogs.append(getHeader("Simulator Output")).append(LINE_SEPARATOR).append(output);
            }
            String error = EMPTY_STRING;
            if (systemError.exists()) error = systemError.loadAsString();
            if (isNotEmpty(error)) {
                finalLogs.append(getHeader("Simulator Errors")).append(LINE_SEPARATOR).append(error);
            }
        } catch (IOException e) {
            finalLogs.append("\n\nFailed to retrieve logs: ").append(getRootCauseMessage(e));
        }
        return MemoryResource.create(finalLogs.toString()).withAttribute(LOG_ATTR, Boolean.TRUE);
    }

    @Override
    public Resource getData() {
        String fileName = toIdentifier(getEnvironment().getName() + "_" + getSimulation().getName()) + ".csv";
        return output.withAttribute(Resource.FILE_NAME_ATTR, fileName);
    }

    @Override
    public Optional<URL> getDashboardUrl() {
        return Optional.empty();
    }

    @Override
    public final Result execute(SimulationContext context) {
        requireNonNull(context);
        this.environment = context.getEnvironment();
        Resource resource = null;
        try {
            resource = doExecute(context);
        } catch (SimulationException e) {
            appendError(e, e.getMessage());
        } catch (Exception e) {
            appendError(e, "Failed to execute simulation ''{0}''", simulation.getName());
        }
        Collection<Output> outputs = Collections.emptyList();
        if (resource != null) {
            try {
                outputs = parseOutput(context, resource);
            } catch (Exception e) {
                appendError(e, "Failed to parse simulation output ''{0}''", simulation.getName());
            }
            try {
                completion();
            } catch (IOException e) {
                appendError(e, "Failed to complete simulation ''{0}''", simulation.getName(), e);
            }
        } else {
            status = Status.FAILED;
        }
        return new SimulationResult(this, outputs);
    }

    @Override
    public void abort() {
        status = Status.CANCELED;
        if (process != null) process.destroyForcibly();
    }

    @Override
    public int compareTo(@NotNull AbstractSimulator o) {
        return id.compareTo(o.id);
    }

    @Override
    public void release() {
        cleanupWorkspace();
    }

    /**
     * Returns the options about simulator (executable, timeouts, etc).
     *
     * @return a non-null instance
     */
    protected final Options getOptions() {
        if (options == null) options = resolveOptions();
        if (options == null) throw new IllegalStateException("Not options are returned by " + ClassUtils.getName(this));
        return options;
    }

    /**
     * Resolves options about simulator.
     *
     * @return a non-null instance
     */
    protected abstract Options resolveOptions();

    /**
     * Parses the output of the executor and returns a simulation output.
     *
     * @param context  the simulation context
     * @param resource the simulator output
     * @return the simulation output
     */
    protected abstract Collection<Output> parseOutput(SimulationContext context, Resource resource) throws IOException;

    /**
     * Updates the process environment with data available in the context.
     *
     * @param process the OS process
     */
    protected void update(ProcessBuilder process, SimulationContext context) {
        // empty by default
    }

    /**
     * Updates the process arguments.
     * <p>
     * Most simulators would accept the input file as the first argument (or maybe with a switch) and dump the result
     * of the simulation to a file.
     *
     * @param arguments the executable arguments
     * @param input     the input of the simulation (script file)
     * @param output    the file where the result of the simulation should be written
     * @param context   the simulation context
     */
    protected abstract void update(List<String> arguments, File input, File output, SimulationContext context);

    /**
     * Invoked at the end of the simulation
     */
    protected void completion() throws IOException {
        // empty by default
    }

    /**
     * Returns the time when the simulation was started.
     *
     * @return a non-null instance
     */
    public final LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the time when the simulation has ended.
     *
     * @return a non-null instance
     */
    public final LocalDateTime getEndTime() {
        return endTime != null ? endTime : LocalDateTime.now();
    }

    /**
     * Returns the duration of the simulation.
     * <p>
     * If the simulation is not completed, it returns the duration of the simulation until now.
     *
     * @return a non-null instance
     */
    public final Duration getDuration() {
        return Duration.between(getStartTime(), getEndTime());
    }

    /**
     * Registers a report.
     *
     * @param resource the content of the report
     */
    protected final void setReport(Resource resource) {
        requireNonNull(resource);
        this.report = resource.withAttribute(REPORT_ATTR, Boolean.TRUE);
    }

    /**
     * Registers a log produced by a simulator.
     * <p>
     * Most of the time the simulator will just output to the standard output and error stream. However, some simulators
     * might create an additional log file.
     *
     * @param resource the content of the report
     */
    protected final void setLog(Resource resource) {
        requireNonNull(resource);
        this.log = resource.withAttribute(LOG_ATTR, Boolean.TRUE);
    }

    /**
     * Returns the workspace of the simulator.
     *
     * @return a non-null instance
     */
    protected final Resource getWorkspace() {
        if (installWorkspace == null) {
            installWorkspace = Resource.workspace().resolve("rest", Resource.Type.DIRECTORY)
                    .resolve("simulator", Resource.Type.DIRECTORY)
                    .resolve(getSimulatorId(), Resource.Type.DIRECTORY);
        }
        return installWorkspace;
    }

    /**
     * Unpacks the simulator into the workspace.
     *
     * @return the directory where the simulator was un-packaged.
     */
    protected final Resource unpack() {
        Resource packageResource = ClassPathResource.file(getOptions().getPackage());
        Resource targetWorkspace = getWorkspace();
        try {
            if (!validatePackage()) {
                log("Unpack simulator to ''{0}''", targetWorkspace.toURI());
                ArchiveInputStream<? extends ArchiveEntry> stream = new ArchiveStreamFactory().createArchiveInputStream(packageResource.getInputStream());
                unpack(targetWorkspace, stream);
                makeExecutable(targetWorkspace);
            }
            return targetWorkspace;
        } catch (ArchiveException e) {
            throw new SimulationException("The simulator package is not an archive: " + packageResource.toURI(), e);
        } catch (Exception e) {
            throw new SimulationException("Failed to unpack the simulator '" + packageResource.getName() + "' to '" + targetWorkspace + "'", e);
        }
    }

    /**
     * Returns the workspace directory where the simulation will store results.
     *
     * @return the resource
     */
    protected final Resource getSessionWorkspace() {
        if (sessionWorkspace == null) {
            sessionWorkspace = Resource.workspace().resolve("rest", Resource.Type.DIRECTORY)
                    .resolve("simulation", Resource.Type.DIRECTORY)
                    .resolve(UUID.randomUUID().toString(), Resource.Type.DIRECTORY);
        }
        return sessionWorkspace;
    }

    /**
     * Logs one line into the logger.
     *
     * @param format the format
     * @param args   the arguments
     * @see java.text.MessageFormat
     */
    protected final void log(String format, Object... args) {
        requireNotEmpty(format);
        if (!logger.isEmpty()) logger.append('\n');
        logger.append(MessageFormat.format(format, args));
    }

    /**
     * Appends a blob of text to the logger.
     *
     * @param text the text to append
     */
    protected final void appendLog(String text) {
        if (text == null) logger.append(text);
    }

    /**
     * Appends an exception to the log
     *
     * @param throwable the exception
     * @param format    the format
     * @param args      the arguments
     */
    protected final void appendError(Throwable throwable, String format, Object... args) {
        errorMessage = getRootCauseMessage(throwable);
        log(format, args);
        appendLog(", stack trace\n" + TextUtils.insertSpaces(ExceptionUtils.getStackTrace(throwable), 5));
    }

    /**
     * Executes the simulation and returns the resource containing the data produced by the simulation.
     *
     * @param context the simulation context
     * @return the data
     */
    private Resource doExecute(SimulationContext context) {
        startTime = LocalDateTime.now();
        try {
            unpack();
            prepareScripts(context);
        } catch (SimulationException e) {
            throw e;
        } catch (Exception e) {
            throw new SimulationException("Failed to prepare simulation '" + simulation.getName() + "'", e);
        }
        try {
            runSimulation(context);
            if (!output.exists()) {
                output = null;
                status = Status.FAILED;
                throw new SimulationException("An output is not available at the end of the simulation of " + simulation.getName());
            }
        } catch (SimulationException e) {
            throw e;
        } catch (Exception e) {
            throw new SimulationException("Failed to execute simulation '" + simulation.getName() + "'", e);
        } finally {
            endTime = LocalDateTime.now();
        }
        return output;
    }

    private void prepareScripts(SimulationContext context) {
        Resource workspace = getSessionWorkspace();
        try {
            log("Prepare script ''{0}'' ({1})", simulation.getName(), formatBytes(simulation.getResource().length()));
            input = copyResource(workspace, simulation, null);
            if (!context.getLibraries().isEmpty()) {
                log("Prepare libraries ({0})", context.getLibraries().size());
            }
            for (Library library : context.getLibraries()) {
                if (!simulation.getProject().equals(library.getProject())) continue;
                log(" - ''{0}'' ({1})", library.getName(), formatBytes(library.getResource().length()));
                copyResource(workspace, library, null);
            }
        } catch (IOException e) {
            throw new SimulationException("Filed to copy simulation or libraries to working space '" + workspace + "'", e);
        }
        output = workspace.resolve(getSimulatorId() + ".csv").withMimeType(MimeType.TEXT_CSV)
                .withAttribute(RestConstants.DATA_ATTR, Boolean.TRUE);
        systemOutput = workspace.resolve(getSimulatorId() + ".system.output");
        systemError = workspace.resolve(getSimulatorId() + ".system.error");
    }

    private void runSimulation(SimulationContext context) {
        List<String> arguments = new ArrayList<>();
        File executable = toFile(getWorkspace().resolve(getOptions().getExecutable()));
        arguments.add(executable.getAbsolutePath());
        update(arguments, toFile(input), toFile(output), context);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments).directory(toFile(getSessionWorkspace()))
                .redirectError(toFile(systemError))
                .redirectOutput(toFile(systemOutput));
        update(processBuilder, context);
        exportEnvironmentVariables(processBuilder, context);
        running = true;
        try {
            log("Execute simulator, command like:\n  " + String.join(SPACE, arguments));
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new SimulationException("Failed to start simulator, executable '" + executable + "'", e);
            }
            boolean timedOut;
            try {
                timedOut = !process.waitFor(simulation.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new SimulationException("Execution of simulator '" + getName() + "' was interrupted", e);
            }
            if (timedOut) {
                throw new SimulationException("Execution of simulator '" + getName() + "' timed out ("
                        + formatDuration(simulation.getTimeout()) + ")");
            }
            int exitValue = process.exitValue();
            if (status == Status.CANCELED) {
                log("Simulation was aborted");
            } else {
                status = exitValue == 0 ? Status.SUCCESSFUL : Status.FAILED;
                if (exitValue != 0) {
                    log("Execution of simulator '" + getName() + "' failed with error code = " + exitValue);
                    throw new SimulationException("Execution of simulator '" + getName() + "' failed with error code = "
                            + exitValue);
                } else {
                    log("Simulation completed successfully in " + formatDuration(Duration.between(getStartTime(), getEndTime())));
                }
            }
        } finally {
            running = false;
        }
    }

    private void exportEnvironmentVariables(ProcessBuilder processBuilder, SimulationContext context) {
        for (Attribute attribute : context.getAttributes()) {
            String name = StringUtils.toIdentifier(attribute.getName()).toUpperCase();
            processBuilder.environment().put(name, ObjectUtils.toString(attribute.getValue()));
        }
    }

    private <S extends ArchiveInputStream<? extends ArchiveEntry>> void unpack(Resource workspace, S stream) throws IOException {
        boolean removeFirstPath = getOptions().isRemoveFirstPath();
        for (; ; ) {
            ArchiveEntry entry = stream.getNextEntry();
            if (entry == null) break;
            if (entry.isDirectory()) continue;
            String path = entry.getName();
            if (removeFirstPath) path = removeFirstPath(path);
            Resource file = workspace.resolve(path);
            OutputStream outputStream = file.getOutputStream();
            StreamUtils.copy(stream, outputStream);
            outputStream.close();
        }
    }

    private String removeFirstPath(String path) {
        String[] parts = StringUtils.split(path, "/");
        parts = ArrayUtils.remove(parts, 0);
        return String.join("/", parts);
    }

    private void makeExecutable(Resource workspace) throws IOException {
        String executable = getOptions().getExecutable();
        File file = ((FileResource) workspace.get(executable)).getFile();
        if (!Files.isExecutable(file.toPath()) && !file.setExecutable(true)) {
            throw new IOException("Executable '" + executable + "' for simulator '" + getName() + "' cannot be made executable");
        }
    }

    private Resource copyResource(Resource workspace, Library library, String fileName) throws IOException {
        Resource resource = library.getResource();
        if (fileName == null) fileName = FileUtils.getFileName(library.getPath());
        return workspace.resolve(fileName).copyFrom(resource);
    }

    private String getSimulatorId() {
        return getOptions().getId();
    }

    private boolean validatePackage() throws IOException {
        if (INSTALLED.contains(getSimulatorId())) return true;
        Resource targetWorkspace = getWorkspace();
        if (!targetWorkspace.exists()) return false;
        Set<String> requiredFiles = getOptions().getRequiredFiles();
        for (String requiredFile : requiredFiles) {
            Resource resource = targetWorkspace.resolve(requiredFile);
            if (!resource.exists()) return false;
        }
        int minimumFileCount = getOptions().getMinimumFileCount();
        if (minimumFileCount > 0) {
            AtomicInteger fileCount = new AtomicInteger();
            targetWorkspace.walk((root, child) -> {
                fileCount.incrementAndGet();
                return true;
            });
            if (fileCount.get() < minimumFileCount) return false;
        }
        INSTALLED.add(getSimulatorId());
        return true;
    }

    private void cleanupWorkspace() {
        if (getStatus() != Status.SUCCESSFUL) return;
        try {
            File directory = toFile(getSessionWorkspace());
            org.apache.commons.io.FileUtils.deleteDirectory(directory);
        } catch (Exception e) {
            log("Failed to cleanup workspace, root cause: {0}", getRootCauseMessage(e));
        }
    }

    private String getLogsAsText() {
        try {
            return getLogs().loadAsString();
        } catch (IOException e) {
            return "#Error: " + getRootCauseMessage(e);
        }
    }

    @Setter
    @Getter
    @ToString
    public static class Options implements Identifiable<String>, Nameable {

        private final String id;
        private String name;
        private String linuxPackage;
        private String windowsPackage;
        private String version = "1.0";
        private String linuxExecutable;
        private String windowsExecutable;
        private boolean removeFirstPath = true;
        private Set<String> requiredFiles = new HashSet<>();
        private int minimumFileCount;

        public Options(String id) {
            this.id = id;
            this.name = capitalizeWords(id);
        }

        public String getPackage() {
            return replaceAll(isWindows() ? getWindowsPackage() : getLinuxPackage(), "${VERSION}", version);
        }

        public Options addFiles(String... files) {
            this.requiredFiles.addAll(Arrays.asList(files));
            return this;
        }

        public Options setMinimumFileCount(int minimumFileCount) {
            this.minimumFileCount = minimumFileCount;
            return this;
        }

        public Options setPackage(String packageName) {
            this.setLinuxPackage(packageName);
            this.setWindowsPackage(packageName);
            return this;
        }

        public String getExecutable() {
            return isWindows() ? getWindowsExecutable() : getLinuxExecutable();
        }

        public Options setExecutable(String executable) {
            this.setLinuxExecutable(executable);
            this.setWindowsExecutable(executable);
            return this;
        }

    }
}
