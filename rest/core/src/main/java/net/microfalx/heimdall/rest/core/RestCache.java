package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Project;
import net.microfalx.heimdall.rest.api.Schedule;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.core.system.*;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MemoryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.CollectionUtils.setFromString;

class RestCache extends ApplicationContextSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestCache.class);

    private volatile Map<String, Project> projects = new HashMap<>();
    private volatile Map<String, Simulation> simulations = new HashMap<>();
    private volatile Map<String, Schedule> schedules = new HashMap<>();
    private volatile Map<String, Library> libraries = new HashMap<>();

    Collection<Project> getProjects() {
        return unmodifiableCollection(projects.values());
    }

    Collection<Simulation> getSimulations() {
        return unmodifiableCollection(simulations.values());
    }

    Collection<Schedule> getSchedules() {
        return unmodifiableCollection(schedules.values());
    }

    Collection<Library> getLibraries() {
        return unmodifiableCollection(libraries.values());
    }

    void registerProject(Project project) {
        projects.put(StringUtils.toIdentifier(project.getId()), project);
    }

    void registerSimulation(Simulation simulation) {
        simulations.put(StringUtils.toIdentifier(simulation.getId()), simulation);
        registerProject(simulation.getProject());
    }

    void registerSchedule(Schedule schedule) {
        schedules.put(StringUtils.toIdentifier(schedule.getId()), schedule);
        registerSimulation(schedule.getSimulation());
    }

    void registerLibrary(Library library) {
        libraries.put(StringUtils.toIdentifier(library.getId()), library);
        registerProject(library.getProject());
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
                    .tags(setFromString(restProject.getTags()))
                    .name(restProject.getName()).description(restProject.getDescription())
                    .id(restProject.getNaturalId());
            Project project = builder.build();
            registerProject(project);
        });
    }

    private void loadLibraries() {
        List<RestLibrary> librariesJPAs = getBean(RestLibraryRepository.class).findAll();
        librariesJPAs.forEach(restLibrary -> {
            Library.Builder builder = new Library.Builder(restLibrary.getNaturalId());
            builder.project(projects.get(restLibrary.getProject().getNaturalId())).type(restLibrary.getType())
                    .resource(MemoryResource.create(restLibrary.getResource()))
                    .tags(setFromString(restLibrary.getTags()))
                    .name(restLibrary.getName()).description(restLibrary.getDescription()).build();
            Library library = builder.build();
            registerLibrary(library);
        });
    }

    private void loadSimulations() {
        List<RestSimulation> simulationJPAs = getBean(RestSimulationRepository.class).findAll();
        simulationJPAs.forEach(restSimulation -> {
            Simulation.Builder builder = new Simulation.Builder();
            builder.project(projects.get(restSimulation.getProject().getNaturalId()))
                    .resource(MemoryResource.create(restSimulation.getResource()))
                    .type(restSimulation.getType()).tag(restSimulation.getTags())
                    .name(restSimulation.getName()).description(restSimulation.getDescription()).build();
            Simulation simulation = builder.build();
            registerSimulation(simulation);
        });
    }

    private void loadSchedules() {
        List<RestSchedule> scheduleJPAs = getBean(RestScheduleRepository.class).findAll();
        scheduleJPAs.forEach(restSimulation -> {
            Environment environment = getBean(InfrastructureService.class).
                    getEnvironment(restSimulation.getEnvironment().getNaturalId());
            Schedule.Builder builder = new Schedule.Builder();
            builder.simulation(simulations.get(restSimulation.getSimulation().getNaturalId()))
                    .environment(environment)
                    .interval(Duration.parse(restSimulation.getInterval())).expression(restSimulation.getExpression())
                    .description(restSimulation.getDescription()).build();
            Schedule schedule = builder.build();
            registerSchedule(schedule);
        });
    }
}
