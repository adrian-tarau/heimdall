package net.microfalx.heimdall.rest.api;

import java.util.Collection;

/**
 * A service which can simulate workloads against HTTP (Restful) endpoints.
 */
public interface RestService {

    /**
     * Returns a collection of registered simulation.
     *
     * @return a non-null instance
     */
    Collection<Simulation> getSimulations();

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
}
