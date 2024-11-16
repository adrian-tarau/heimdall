package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.TimeUtils.ONE_MINUTE;

/**
 * A manager for  {@link net.microfalx.heimdall.rest.api.Project}.
 * <p>
 * Projects have references to the repository where they reside, and simulation scripts (and libraries) are loaded
 * from various parts of the project.
 */
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
}
