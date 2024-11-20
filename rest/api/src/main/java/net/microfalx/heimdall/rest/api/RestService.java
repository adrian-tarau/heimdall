package net.microfalx.heimdall.rest.api;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.Collection;

/**
 * A service which can simulate workloads against HTTP (Restful) endpoints.
 */
public interface RestService {

    /**
     * Returns a collection of registered projects].
     *
     * @return a non-null instance
     */
    Collection<Project> getProjects();

    /**
     * Registers a project.
     *
     * @param project the project
     */
    void registerProject(Project project);

    /**
     * Returns a project by its identifier.
     *
     * @param id the project identifier.
     * @return the project
     * @throws RestNotFoundException if such a project does not exist
     */
    Project getProject(String id);

    /**
     * Returns a collection of registered simulations.
     *
     * @return a non-null instance
     */
    Collection<Simulation> getSimulations();

    /**
     * Returns a simulation by its identifier.
     *
     * @param id the simulation identifier.
     * @return the simulation
     * @throws RestNotFoundException if such a simulation does not exist
     */
    Simulation getSimulation(String id);

    /**
     * Registers a simulation.
     *
     * @param simulation the simulation
     */
    void registerSimulation(Simulation simulation);

    /**
     * Returns a collection of registered schedules.
     *
     * @return a non-null instance
     */
    Collection<Schedule> getSchedules();

    /**
     * Returns a schedule by its identifier.
     *
     * @param id the schedule identifier.
     * @return the schedule
     * @throws RestNotFoundException if such a schedule does not exist
     */
    Schedule getSchedule(String id);

    /**
     * Returns a collection of libraries supporting the simulations.
     *
     * @return a non-null instance
     */
    Collection<Library> getLibraries();

    /**
     * Registers a library.
     *
     * @param library the library
     */
    void registerLibrary(Library library);

    /**
     * Registers a resource to be stored for later use.
     * <p>
     * The resource is the byproduct of a simulation. Each resource will be saved in different storage,
     * based on the attributes attached to the resource.
     *
     * @param resource the resource
     * @return a new resource
     * @throws IOException if an I/O error occurs
     * @see RestConstants constants ending in <code>_ATTR</code>
     */
    Resource registerResource(Resource resource) throws IOException;

    /**
     * Returns a simulation based on the provided script.
     *
     * @param resource the resource
     * @return a non-null instance
     */
    Simulation discover(Resource resource);

    /**
     * Returns the running simulators.
     * @return a non-null instance
     */
    Collection<Simulator> getRunning();

    /**
     * Returns the last N (100 by default) simulators.
     *
     * @return a non-null instance
     */
    Collection<Simulator> getHistory();

    /**
     * Executes a simulation.
     *
     * @param simulation the simulation
     * @return future which tracks the execution of the simulation
     */
    Result simulate(Simulation simulation, Environment environment);

    /**
     * Reloads the rest definitions.
     */
    void reload();
}
