package net.microfalx.heimdall.rest.api;

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
     * Returns a collection of registered simulation.
     *
     * @return a non-null instance
     */
    Collection<Simulation> getSimulations();

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
     *
     * @param resource the resource
     * @return a new resource
     * @throws IOException if an I/O error occurs
     */
    Resource registerResource(Resource resource) throws IOException;

    /**
     * Returns a simulation based on the provided scrip
     *
     * @param resource the resource
     * @return a non-null instance
     */
    Simulation discover(Resource resource);

    /**
     * Reloads the rest definitions.
     */
    void reload();
}
