package net.microfalx.heimdall.rest.api;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;

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
    Collection<Scenario> getScenarios();

    /**
     * Returns a simulation by its identifier.
     *
     * @param id the simulation identifier.
     * @return the simulation
     * @throws RestNotFoundException if such a simulation does not exist
     */
    Scenario getScenario(String id);

    /**
     * Returns the simulation by its name.
     * <p>
     * If the scenario does not exist, it is registered.
     *
     * @param simulation the simulation which owns the scenario
     * @param name       the name of the scenario
     * @return the scenario
     * @throws RestNotFoundException if such a simulation does not exist
     */
    Scenario getScenario(Simulation simulation, String name);

    /**
     * Registers a simulation.
     *
     * @param scenario the scenario
     */
    void registerScenario(Scenario scenario);

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
     * Returns a library by its identifier.
     *
     * @param id the library identifier.
     * @return the library
     * @throws RestNotFoundException if such a library does not exist
     */
    Library getLibrary(String id);

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
     *
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
     * Creates a simulation context only on system defined libraries.
     *
     * @param environment the environment
     * @param simulation  the simulation
     * @return a non-null instance
     * @see #createContext(Environment, Simulation, Collection)
     */
    SimulationContext createContext(Environment environment, Simulation simulation);

    /**
     * Creates a simulation context.
     * <p>
     * If the simulation comes from a code repository, the simulation is refreshed and a newer version is used, if
     * available in the database.
     *
     * @param environment the environment
     * @param simulation  the simulation
     * @param libraries   the libraries
     * @return a non-null instance
     */
    SimulationContext createContext(Environment environment, Simulation simulation, Collection<Library> libraries);

    /**
     * Executes a simulation.
     *
     * @param context the simulation context
     * @return future which tracks the execution of the simulation
     */
    Result simulate(SimulationContext context);

    /**
     * Schedules a simulation.
     *
     * @param context the simulation context
     * @return future which tracks the execution of the simulation
     */
    Future<Result> schedule(SimulationContext context);

    /**
     * Returns the last time when a simulation was executed for a given environment.
     *
     * @param simulation  the simulation
     * @param environment the environment
     * @return a non-null instance
     */
    Optional<LocalDateTime> getLastRun(Simulation simulation, Environment environment);

    /**
     * Returns the log for a given simulation.
     *
     * @param id the identifier of the result
     * @return a non-null instance
     */
    Resource getLog(int id);

    /**
     * Returns the report for a given simulation.
     *
     * @param id the identifier of the result
     * @return a non-null instance
     */
    Resource getReport(int id);

    /**
     * Returns the live report for a given simulation.
     *
     * @param simulator the simulator
     * @return a non-null instance
     */
    Resource getReport(Simulator simulator);

    /**
     * Returns the data for a given simulation.
     *
     * @param id the identifier of the result
     * @return a non-null instance
     */
    Resource getData(int id);

    /**
     * Reloads all projects.
     */
    void reload();

    /**
     * Reloads a project.
     *
     * @param project the project
     */
    void reload(Project project);

    /**
     * Reloads a schedule.
     *
     * @param schedule the schedule
     */
    void reload(Schedule schedule);
}
