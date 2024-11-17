package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;

import java.io.File;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.TimeUtils.ONE_MINUTE;

/**
 * A manager for  {@link net.microfalx.heimdall.rest.api.Project}.
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
    }

    /**
     * Reloads a project.
     * <p>
     * The project is validated (every 60s) if the repository has a different version compared with local repository.
     *
     * @param project the project
     */
    void reload(Project project) {
        requireNonNull(project);
        synchronize(project);
        discover(project);
    }

    /**
     * Clones or updates a project in the local file system
     *
     * @param project the project
     */
    private void synchronize(Project project) {

    }

    /**
     * Scans the project directory for libraries and simulations.
     *
     * @param project the project
     */
    private void discover(Project project) {

    }

    /**
     * Returns the directory in the local file system which will contain the repository.
     *
     * @param project the project
     * @return the directory
     */
    private File getWorkspace(Project project) {
        return new File(ResourceUtils.toFile(this.projectResource), project.getId());
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
