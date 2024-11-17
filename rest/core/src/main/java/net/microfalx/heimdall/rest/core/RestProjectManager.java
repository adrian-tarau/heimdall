package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.StringUtils.SPACE;
import static net.microfalx.lang.TimeUtils.ONE_MINUTE;
import static net.microfalx.resource.ResourceUtils.toFile;

/**
 * A manager for  {@link Project}.
 * <p>
 * Projects have references to the repository where they reside, and simulation scripts (and libraries) are loaded
 * from various parts of the project.
 */
@Slf4j
public class RestProjectManager {

    private static final long RELOAD_INTERVAL = ONE_MINUTE;

    private RestServiceImpl restService;
    private Resource projectResource;

    /**
     * Reloads projects from the database.
     */
    void reload() {
        for (Project project : restService.getProjects()) {
            try {
                reload(project);
            } catch (Exception e) {
                LOGGER.error("Failed to update project '" + project.getName() + "'", e);
            }
        }
    }

    /**
     * Reloads a project.
     * <p>
     * The project is validated (every 60s) if the repository has a different version compared with local repository.
     *
     * @param project the project
     */
    void reload(Project project) throws IOException {
        requireNonNull(project);
        synchronize(project);
        discover(project);
    }

    /**
     * Clones or updates a project in the local file system
     *
     * @param project the project
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
     * @throws IOException
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
            LOGGER.info("Execute command '{}'", command);
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
                throw new IOException("Execution of '" + command + "' failed with error code = " + exitValue);
            } else {
                LOGGER.info("The command was executed successfully");
            }
        } finally {
            try {
                process.destroy();
            } catch (Exception e) {
                LOGGER.warn("Failed to destroy the process: " + ExceptionUtils.getRootCauseMessage(e));
            }
            if (error.exists()) error.delete();
            if (output.exists()) output.delete();
        }
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
                    arguments.add(project.getUri().toASCIIString());
                    arguments.add(project.getId());
                }
                break;
            case SVN:
                arguments.add("-q");
                if (isUpdate) {
                    arguments.add("update");
                } else {
                    arguments.add("checkout");
                    arguments.add(project.getUri().toASCIIString());
                    arguments.add(project.getId());
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
     */
    private void discover(Project project) {
        File directory = getWorkspace(project);

    }

    /**
     * Returns the directory in the local file system which will contain the repository.
     *
     * @param project the project
     * @return the directory
     */
    private File getWorkspace(Project project) {
        return new File(getWorkspace(), project.getId());
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
     */
    private void discoverDirectories(Project project, DiscoveryType discoveryType, String pathPattern) {
    }

    /**
     * Discovers a library and simulation.
     *
     * @param resource      the file resource
     * @param discoveryType the enum which tells if we discover a library or a simulation
     */
    private void discoverFile(Resource resource, DiscoveryType discoveryType) {

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
