package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.UriUtils;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MINUTES;
import static net.microfalx.heimdall.rest.api.RestConstants.SCRIPT_ATTR;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.StringUtils.SPACE;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.TextUtils.insertSpaces;
import static net.microfalx.lang.TimeUtils.*;
import static net.microfalx.resource.ResourceUtils.toFile;

/**
 * A manager for  {@link Project}.
 * <p>
 * Projects have references to the repository where they reside, and simulation scripts (and libraries) are loaded
 * from various parts of the project.
 */
@Slf4j
public class RestProjectManager {

    private static final long RELOAD_INTERVAL = FIVE_MINUTE;

    private RestServiceImpl restService;
    private Resource projectResource;
    private final Map<String, Long> lastReload = new ConcurrentHashMap<>();
    private final Map<String, Lock> lock = new ConcurrentHashMap<>();

    /**
     * Reloads projects from the database.
     */
    void reload() {
        for (Project project : restService.getProjects()) {
            try {
                reload(project);
            } catch (Exception e) {
                LOGGER.error("Failed to reload project '" + project.getName() + "'", e);
            }
        }
    }

    /**
     * Reloads a project.
     * <p>
     * The project is validated (every 60s) if the repository has a different version compared with local repository.
     *
     * @param project the project
     * @throws IOException if an I/O error occurs
     */
    void reload(Project project) throws IOException {
        requireNonNull(project);
        if (project.getType() == Project.Type.NONE) return;
        Lock lock = getLock(project);
        if (!lock.tryLock()) return;
        try {
            Long lastLoaded = lastReload.computeIfAbsent(project.getId(), k -> oneHourAgo());
            if (millisSince(lastLoaded) > RELOAD_INTERVAL) {
                try {
                    synchronize(project);
                    discover(project);
                } finally {
                    lastReload.put(project.getId(), currentTimeMillis());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clones or updates a project in the local file system
     *
     * @param project the project
     * @throws IOException if an I/O error occurs
     */
    private void synchronize(Project project) throws IOException {
        List<String> arguments = new ArrayList<>();
        arguments.add(getExecutable(project));
        updateArguments(project, arguments);
        execute(project, arguments);
    }

    /**
     * Executes the OS process to update or checkout a project.
     *
     * @param project   the project
     * @param arguments the source control tool + arguments
     * @throws IOException if an I/O error occurs
     */
    private void execute(Project project, List<String> arguments) throws IOException {
        File workspace = getWorkspace(project);
        if (!workspace.exists()) workspace = getWorkspace();
        File output = File.createTempFile(project.getId(), ".out");
        File error = File.createTempFile(project.getId(), ".error");
        String command = String.join(SPACE, arguments);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments).directory(workspace)
                .redirectError(error).redirectOutput(output);
        Process process = null;
        try {
            LOGGER.info("Execute command '{}' for project '{}'", command, project.getName());
            process = processBuilder.start();
            boolean timedOut = false;
            try {
                timedOut = !process.waitFor(5, MINUTES);
            } catch (InterruptedException e) {
                ExceptionUtils.rethrowInterruptedException(e);
            }
            if (timedOut) {
                throw new IOException("Timeout wait waiting for process to finish");
            }
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new IOException("Execution of '" + command + "' failed with error code = " + exitValue
                        + " for project " + project.getName() + ", logs\n" + insertSpaces(getLogs(output, error), 5));
            } else {
                LOGGER.info("The command was executed successfully");
            }
        } finally {
            try {
                if (process != null) process.destroy();
            } catch (Exception e) {
                LOGGER.warn("Failed to destroy the process: {}", getRootCauseMessage(e));
            }
            if (error.exists()) error.delete();
            if (output.exists()) output.delete();
        }
    }

    private String getLogs(File output, File error) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(Resource.file(output).loadAsString()).append('\n');
            builder.append(Resource.file(error).loadAsString()).append('\n');
        } catch (IOException e) {
            builder.append("Error: ").append(ExceptionUtils.getRootCauseMessage(e));
        }
        return builder.toString();
    }

    private void updateArguments(Project project, List<String> arguments) {
        boolean isUpdate = getWorkspace(project).exists();
        switch (project.getType()) {
            case GIT:
                if (isUpdate) {
                    arguments.add("pull");
                    arguments.add("-q");
                } else {
                    arguments.add("clone");
                    arguments.add("-q");
                    arguments.add(getGitUri(project).toASCIIString());
                    arguments.add(getDirectoryName(project));
                }
                break;
            case SVN:
                arguments.add("-q");
                if (isUpdate) {
                    arguments.add("update");
                } else {
                    arguments.add("checkout");
                    arguments.add(project.getUri().toASCIIString());
                    arguments.add(getDirectoryName(project));
                }
                break;
        }
    }

    private String getExecutable(Project project) {
        return project.getType() == Project.Type.GIT ? "git" : "svn";
    }

    /**
     * Scans the project directory for libraries and simulations.
     *
     * @param project the project
     * @throws IOException if an I/O error occurs
     */
    private void discover(Project project) throws IOException {
        discover(project, DiscoveryType.LIBRARY, project.getLibraryPath());
        discover(project, DiscoveryType.SIMULATION, project.getSimulationPath());
    }

    /**
     * Returns the directory in the local file system which will contain the repository.
     *
     * @param project the project
     * @return the directory
     */
    private File getWorkspace(Project project) {
        return new File(getWorkspace(), getDirectoryName(project));
    }

    /**
     * Returns the directory in the local file system which will contain all the repositories.
     *
     * @return a non-null instance
     */
    private File getWorkspace() {
        return validateDirectoryExists(toFile(this.projectResource));
    }

    /**
     * Finds directories which will container libraries or simulations for a given project with a given pattern.
     *
     * @param project       the project
     * @param discoveryType the enum which tells if we discover a library or a simulation
     * @param pathPattern   an Ant path matcher
     * @throws IOException if an I/O error occurs
     */
    private void discover(Project project, DiscoveryType discoveryType, String pathPattern) throws IOException {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        String[] patterns = StringUtils.split(pathPattern, Library.PATH_SEPARATORS);
        Resource directory = FileResource.directory(getWorkspace(project));
        directory.walk((root, child) -> {
            if (child.isFile()) {
                String resourcePath = child.getPath(root);
                child = child.withAttribute(Resource.PATH_ATTR, resourcePath);
                discover(project, discoveryType, child, pathMatcher, patterns);
            }
            return true;
        });
    }

    /**
     * Validates whether the resource matches the required file patters.
     *
     * @param project       the project
     * @param discoveryType the enum which tells if we discover a library or a simulation
     * @param resource      the resource
     * @param pathMatcher   the path matcher
     * @param patterns      the patterns to validate files
     * @throws IOException if an I/O error occurs
     */
    private void discover(Project project, DiscoveryType discoveryType, Resource resource, PathMatcher pathMatcher, String[] patterns) throws IOException {
        String resourcePath = resource.getPath(true);
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, resourcePath)) {
                try {
                    discover(project, resource, discoveryType);
                } catch (SimulationException e) {
                    LOGGER.warn("Invalid script type for '" + resourcePath + "', root cause: " + getRootCauseMessage(e));
                }
            }
        }
    }

    /**
     * Discovers a library and simulation.
     *
     * @param resource      the file resource
     * @param discoveryType the enum which tells if we discover a library or a simulation
     * @throws IOException if an I/O error occurs
     */
    private void discover(Project project, Resource resource, DiscoveryType discoveryType) throws IOException {
        Simulation discoveredSimulation = restService.discover(resource);
        Resource storedResource = restService.registerResource(resource.withAttribute(SCRIPT_ATTR, Boolean.TRUE));
        String id = getId(project, discoveredSimulation.getType(), resource);
        if (discoveryType == DiscoveryType.LIBRARY) {
            Library.Builder builder = new Library.Builder(id).project(project)
                    .resource(storedResource);
            builder.path(resource.getPath(true)).tags(discoveredSimulation.getTags())
                    .name(discoveredSimulation.getName()).description(discoveredSimulation.getDescription());
            builder.type(discoveredSimulation.getType());
            Library library = builder.build();
            if (!canRegister(library)) restService.registerLibrary(library);
        } else if (discoveryType == DiscoveryType.SIMULATION) {
            Simulation.Builder builder = new Simulation.Builder(id);
            builder.project(project).resource(storedResource);
            builder.path(resource.getPath(true)).tags(discoveredSimulation.getTags())
                    .name(discoveredSimulation.getName()).description(discoveredSimulation.getDescription());
            builder.type(discoveredSimulation.getType());
            Simulation simulation = builder.build();
            if (canRegister(simulation)) restService.registerSimulation(simulation);
        } else {
            throw new IllegalStateException("Unhandled discovery type: " + discoveryType);
        }
    }

    private boolean canRegister(Library newLibrary) {
        requireNonNull(newLibrary);
        Library currentLibrary = null;
        try {
            if (newLibrary instanceof Simulation) {
                currentLibrary = restService.getSimulation(newLibrary.getId());
            } else {
                currentLibrary = restService.getLibrary(newLibrary.getId());
            }
        } catch (RestNotFoundException e) {
            // does not exist
        }
        if (currentLibrary != null && Boolean.TRUE.equals(currentLibrary.getOverride())) return false;
        try {
            return !ResourceUtils.hasSameContent(newLibrary.getResource(), currentLibrary.getResource());
        } catch (IOException e) {
            LOGGER.error("Failed to compare resources for library '{}'", newLibrary.getName());
            return false;
        }
    }

    private String getId(Project project, Simulation.Type type, Resource resource) {
        return Library.getNaturalId(type, resource, project.getId());
    }

    private URI getGitUri(Project project) {
        URI uri = project.getUri();
        if (isNotEmpty(project.getToken())) {
            StringBuilder builder = new StringBuilder();
            builder.append(uri.getScheme()).append("://")
                    .append(project.getUserName()).append(':').append(project.getToken()).append('@').append(uri.getHost());
            if (uri.getPort() > 0) builder.append(":").append(uri.getPort());
            builder.append(uri.getPath());
            return UriUtils.parseUri(builder.toString());
        } else {
            return uri;
        }
    }

    private String getDirectoryName(Project project) {
        return StringUtils.toIdentifier(project.getName()) + "_" + project.getId();
    }

    private Lock getLock(Project project) {
        return lock.computeIfAbsent(project.getId(), s -> new ReentrantLock());
    }

    /**
     * Receives the required dependencies and reloads the projects for the first time.
     *
     * @param restService the rest service
     */
    void initialize(RestServiceImpl restService) {
        this.restService = restService;
        this.projectResource = restService.getProjectResource();
        this.reload();
    }

    private enum DiscoveryType {
        LIBRARY, SIMULATION
    }
}
