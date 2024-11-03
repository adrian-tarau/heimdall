package net.microfalx.heimdall.rest.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.lang.*;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.lang.JvmUtils.isWindows;
import static net.microfalx.lang.StringUtils.capitalizeWords;
import static net.microfalx.lang.StringUtils.replaceAll;
import static net.microfalx.resource.ResourceUtils.toFile;

/**
 * Base class for the simulator.
 */
@Slf4j
public abstract class AbstractSimulator implements Simulator, Identifiable<String>, Nameable {

    private final Simulation simulation;
    private Options options;
    private Resource installWorkspace;
    private Resource sessionWorkspace;
    private Resource input;
    private Resource systemOutput;
    private Resource systemError;
    private Resource output;

    private static final Set<String> INSTALLED = new CopyOnWriteArraySet<>();

    public AbstractSimulator(Simulation simulation) {
        requireNonNull(simulation);
        this.simulation = simulation;
    }

    @Override
    public final String getId() {
        return getOptions().getId();
    }

    @Override
    public final String getName() {
        return getOptions().getName();
    }

    @Override
    public final Simulation getSimulation() {
        return simulation;
    }

    @Override
    public final Collection<Output> execute(SimulationContext context) {
        Resource resource = doExecute(context);
        try {
            return parseOutput(context, resource);
        } catch (Exception e) {
            throw new SimulationException("Failed to parse simulation output '" + simulation.getName() + "'", e);
        }
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
     * Returns the workspace of the simulator.
     *
     * @return a non-null instance
     */
    protected final Resource getWorkspace() {
        if (installWorkspace == null) {
            installWorkspace = Resource.workspace().resolve("simulator", Resource.Type.DIRECTORY)
                    .resolve(getOptions().getId(), Resource.Type.DIRECTORY);
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

    protected final Resource getSessionWorkspace() {
        if (sessionWorkspace == null)
            sessionWorkspace = Resource.workspace().resolve("simulation", Resource.Type.DIRECTORY)
                    .resolve(UUID.randomUUID().toString(), Resource.Type.DIRECTORY);
        return sessionWorkspace;
    }

    /**
     * Executes the simulation and returns the resource containing the data produced by the simulation.
     *
     * @param context the simulation context
     * @return the data
     */
    private Resource doExecute(SimulationContext context) {
        try {
            unpack();
            prepareScripts(context);
            runSimulation(context);
        } catch (SimulationException e) {
            throw e;
        } catch (Exception e) {
            throw new SimulationException("Failed to prepare simulation '" + simulation.getName() + "'", e);
        }
        try {
            if (!output.exists()) {
                throw new SimulationException("An output is not available at the end of the simulation of " + simulation.getName());
            }
        } catch (SimulationExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new SimulationExecutionException("Failed to execute simulation '" + simulation.getName() + "'", e);
        }
        return output;
    }

    private void prepareScripts(SimulationContext context) {
        Resource workspace = getSessionWorkspace();
        try {
            input = copyResource(workspace, simulation.getResource());
            for (Library library : context.getLibraries()) {
                copyResource(workspace, library.getResource());
            }
        } catch (IOException e) {
            throw new SimulationException("Filed to copy simulation or libraries to working space '" + workspace + "'", e);
        }
        String simulationFileName = FileUtils.removeFileExtension(simulation.getResource().getFileName());
        output = workspace.resolve(simulationFileName + ".data");
        systemOutput = workspace.resolve(simulationFileName + ".system.output");
        systemError = workspace.resolve(simulationFileName + ".system.error");
    }

    private void runSimulation(SimulationContext context) {
        List<String> arguments = new ArrayList<>();
        File executable = toFile(getWorkspace().resolve(getOptions().getExecutable()));
        arguments.add(executable.getAbsolutePath());
        update(arguments, toFile(input), toFile(output), context);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments).directory(toFile(getSessionWorkspace()))
                .redirectError(toFile(systemOutput))
                .redirectOutput(toFile(systemError));
        update(processBuilder, context);
        exportEnvironmentVariables(processBuilder, context);
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new SimulationExecutionException("Failed to start simulator, executable '" + executable + "'", e);
        }
        boolean timedOut;
        try {
            timedOut = !process.waitFor(simulation.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new SimulationExecutionException("Execution of simulator '" + getName() + "' was interrupted", e);
        }
        if (timedOut) {
            throw new SimulationExecutionException("Execution of simulator '" + getName() + "' timed out ("
                    + formatDuration(simulation.getTimeout()) + ")");
        }
        int exitValue = process.exitValue();
        if (exitValue != 0) {
            throw new SimulationExecutionException("Execution of simulator '" + getName() + "' failed with error code = "
                    + exitValue + ", error stream: " + getErrorOutput());
        }
    }

    private void exportEnvironmentVariables(ProcessBuilder processBuilder, SimulationContext context) {
        for (Attribute attribute : context.getEnvironment().getAttributes()) {
            String name = StringUtils.toIdentifier(attribute.getName()).toUpperCase();
            processBuilder.environment().put(name, ObjectUtils.toString(attribute.getValue()));
        }
    }

    private String getErrorOutput() {
        try {
            if (systemError.exists()) {
                return systemError.loadAsString();
            } else if (systemOutput.exists()) {
                return systemOutput.loadAsString();
            } else {
                return "N/A";
            }
        } catch (IOException e) {
            return "Error: " + ExceptionUtils.getRootCauseMessage(e);
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

    private Resource copyResource(Resource workspace, Resource script) throws IOException {
        return workspace.resolve(script.getFileName()).copyFrom(script);
    }

    private boolean validatePackage() throws IOException {
        if (INSTALLED.contains(getId())) return true;
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
        INSTALLED.add(getId());
        return true;
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
