package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.heimdall.rest.core.system.*;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.CollectionUtils.setFromString;
import static net.microfalx.lang.TimeUtils.parseDuration;

class RestCache extends ApplicationContextSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestCache.class);

    private volatile Map<String, Project> projects = new HashMap<>();
    private volatile Map<String, Simulation> simulations = new HashMap<>();
    private volatile Map<String, Schedule> schedules = new HashMap<>();
    private volatile Map<String, Library> libraries = new HashMap<>();

    Collection<Project> getProjects() {
        return unmodifiableCollection(projects.values());
    }

    Project getProject(String id) {
        Project project = projects.get(StringUtils.toIdentifier(id));
        if (project == null) throw new RestNotFoundException("A project with identifier '" + id + "' does not exist");
        return project;
    }

    Collection<Simulation> getSimulations() {
        return unmodifiableCollection(simulations.values());
    }

    Simulation getSimulation(String id) {
        Simulation simulation = simulations.get(StringUtils.toIdentifier(id));
        if (simulation == null) throw new RestNotFoundException("A simulation with identifier '" + id + "' does not exist");
        return simulation;
    }

    Collection<Schedule> getSchedules() {
        return unmodifiableCollection(schedules.values());
    }

    Schedule getSchedule(String id) {
        Schedule schedule = schedules.get(StringUtils.toIdentifier(id));
        if (schedule == null) throw new RestNotFoundException("A schedule with identifier '" + id + "' does not exist");
        return schedule;
    }

    Collection<Library> getLibraries() {
        return unmodifiableCollection(libraries.values());
    }

    void registerProject(Project project, Integer key) {
        projects.put(StringUtils.toIdentifier(project.getId()), project);
        if (key != null) projects.put(Integer.toString(key), project);
    }

    void registerSimulation(Simulation simulation, Integer key) {
        simulations.put(StringUtils.toIdentifier(simulation.getId()), simulation);
        if (key != null) simulations.put(Integer.toString(key), simulation);
    }

    void registerSchedule(Schedule schedule, Integer key) {
        schedules.put(StringUtils.toIdentifier(schedule.getId()), schedule);
        if (key != null) schedules.put(Integer.toString(key), schedule);
    }

    void registerLibrary(Library library, Integer key) {
        libraries.put(StringUtils.toIdentifier(library.getId()), library);
        if (key != null) libraries.put(Integer.toString(key), library);
    }

    void load() {
        try {
            loadProjects();
        } catch (Exception e) {
            LOGGER.error("Failed to load projects", e);
        }
        try {
            loadLibraries();
        } catch (Exception e) {
            LOGGER.error("Failed to load libraries", e);
        }
        try {
            loadSimulations();
        } catch (Exception e) {
            LOGGER.error("Failed to load simulations", e);
        }
        try {
            loadSchedules();
        } catch (Exception e) {
            LOGGER.error("Failed to load schedules", e);
        }
        LOGGER.info("projects: {}, libraries: {}, simulations: {}, schedules: {}",
                projects.size(), libraries.size(), simulations.size(), schedules.size());
    }

    private void loadProjects() {
        List<RestProject> projectsJPAs = getBean(RestProjectRepository.class).findAll();
        projectsJPAs.forEach(restProject -> {
            Project.Builder builder = new Project.Builder(URI.create(restProject.getUri()));
            builder.userName(restProject.getUserName()).password(restProject.getPassword())
                    .token(restProject.getToken()).type(restProject.getType())
                    .libraryPath(restProject.getLibraryPath()).simulationPath(restProject.getSimulationPath())
                    .tags(setFromString(restProject.getTags()))
                    .name(restProject.getName()).description(restProject.getDescription())
                    .id(restProject.getNaturalId());
            Project project = builder.build();
            registerProject(project, restProject.getId());
        });
    }

    private void loadLibraries() {
        List<RestLibrary> librariesJPAs = getBean(RestLibraryRepository.class).findAll();
        librariesJPAs.forEach(restLibrary -> {
            Resource resource = ResourceFactory.resolve(restLibrary.getResource());
            Library.Builder builder = new Library.Builder(restLibrary.getNaturalId());
            RestProject restProject = restLibrary.getProject();
            if (restProject != null) builder.project(getProject(restProject.getNaturalId()));
            builder.type(restLibrary.getType())
                    .resource(resource).path(restLibrary.getPath())
                    .tags(setFromString(restLibrary.getTags()))
                    .name(restLibrary.getName()).description(restLibrary.getDescription()).build();
            Library library = builder.build();
            registerLibrary(library, restLibrary.getId());
        });
    }

    private void loadSimulations() {
        List<RestSimulation> simulationJPAs = getBean(RestSimulationRepository.class).findAll();
        simulationJPAs.forEach(restSimulation -> {
            Resource resource = ResourceFactory.resolve(restSimulation.getResource());
            Simulation.Builder builder = new Simulation.Builder(restSimulation.getNaturalId());
            RestProject restProject = restSimulation.getProject();
            if (restProject != null) builder.project(getProject(restProject.getNaturalId()));
            builder.timeout(ofSeconds(restSimulation.getTimeout()));
            builder.resource(resource).path(restSimulation.getPath())
                    .type(restSimulation.getType()).tag(restSimulation.getTags())
                    .name(restSimulation.getName()).description(restSimulation.getDescription()).build();
            Simulation simulation = builder.build();
            registerSimulation(simulation, restSimulation.getId());
        });
    }

    private void loadSchedules() {
        List<RestSchedule> scheduleJPAs = getBean(RestScheduleRepository.class).findAll();
        scheduleJPAs.forEach(restSchedule -> {
            Environment environment = getBean(InfrastructureService.class).
                    getEnvironment(restSchedule.getEnvironment().getNaturalId());
            Schedule.Builder builder = new Schedule.Builder(Integer.toString(restSchedule.getId()));
            builder.simulation(getSimulation(restSchedule.getSimulation().getNaturalId())).environment(environment);
            switch (restSchedule.getType()) {
                case EXPRESSION:
                    builder.expression(restSchedule.getExpression());
                    break;
                case INTERVAL:
                    builder.interval(parseDuration(restSchedule.getInterval()));
                    break;
            }
            builder.description(restSchedule.getDescription()).build();
            Schedule schedule = builder.build();
            registerSchedule(schedule, restSchedule.getId());
        });
    }
}
