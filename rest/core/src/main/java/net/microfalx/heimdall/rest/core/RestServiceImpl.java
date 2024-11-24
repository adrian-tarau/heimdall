package net.microfalx.heimdall.rest.core;


import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.heimdall.rest.core.system.RestResult;
import net.microfalx.heimdall.rest.core.system.RestResultRepository;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.UriUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import net.microfalx.resource.rocksdb.RocksDbResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.heimdall.rest.api.RestConstants.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.NamedAndTaggedIdentifyAware.*;

@Service
@Slf4j
public class RestServiceImpl implements RestService, InitializingBean {

    private volatile RestCache cache = new RestCache();

    @Autowired
    private RestProperties properties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private RestResultRepository restResultRepository;

    @Autowired
    private TaskExecutor taskExecutor;

    private final RestProjectManager projectManager = new RestProjectManager();
    private final RestSimulationScheduler simulationScheduler = new RestSimulationScheduler();

    private final RestPersistence persistence = new RestPersistence();
    private final Collection<Simulation.Provider> simulationProviders = new CopyOnWriteArrayList<>();
    private final Collection<Simulator.Provider> simulatorProviders = new CopyOnWriteArrayList<>();
    private final Set<Simulator> running = new ConcurrentSkipListSet<>();
    private final Queue<Simulator> history = new ConcurrentLinkedQueue<>();
    private Resource scriptResource;
    private Resource logsResource;
    private Resource reportResource;
    private Resource projectResource;

    @Override
    public Collection<Project> getProjects() {
        return cache.getProjects();
    }

    @Override
    public Project getProject(String id) {
        requireNonNull(id);
        return cache.getProject(id);
    }

    @Override
    public void registerProject(Project project) {
        requireNonNull(project);
        cache.registerProject(project, null);
        persistence.save(project);
    }

    @Override
    public Collection<Simulation> getSimulations() {
        return cache.getSimulations();
    }

    @Override
    public Simulation getSimulation(String id) {
        requireNonNull(id);
        return cache.getSimulation(id);
    }

    @Override
    public void registerSimulation(Simulation simulation) {
        requireNonNull(simulation);
        cache.registerSimulation(simulation, null);
        persistence.save(simulation);
    }

    @Override
    public Collection<Schedule> getSchedules() {
        return cache.getSchedules();
    }

    @Override
    public Schedule getSchedule(String id) {
        requireNonNull(id);
        return cache.getSchedule(id);
    }

    @Override
    public Collection<Library> getLibraries() {
        return cache.getLibraries();
    }

    @Override
    public void registerLibrary(Library library) {
        requireNonNull(library);
        cache.registerLibrary(library, null);
        persistence.save(library);
    }

    @Override
    public Resource registerResource(Resource resource) throws IOException {
        requireNonNull(resource);
        if (!resource.exists()) return resource;
        Resource target;
        if (resource.hasAttribute(SCRIPT_ATTR)) {
            target = scriptResource;
        } else if (resource.hasAttribute(LOG_ATTR)) {
            target = logsResource;
        } else if (resource.hasAttribute(REPORT_ATTR)) {
            target = reportResource;
        } else {
            throw new RestException("Expected attributes not found for resource " + resource.toURI());
        }
        target = target.resolve(resource.toHash());
        target.copyFrom(resource);
        return target;
    }

    @Override
    public Simulation discover(Resource resource) {
        requireNonNull(resource);
        for (Simulation.Provider provider : simulationProviders) {
            if (provider.supports(resource)) {
                return provider.create(resource);
            }
        }
        throw new SimulationException("A simulation cannot be provided for script: " + resource.getName());
    }

    @Override
    public Collection<Simulator> getRunning() {
        return unmodifiableCollection(running);
    }

    @Override
    public Collection<Simulator> getHistory() {
        return unmodifiableCollection(history);
    }

    @Override
    public Result simulate(Simulation simulation, Environment environment) {
        requireNonNull(simulation);
        requireNonNull(environment);
        Simulator simulator = createSimulator(simulation);
        running.add(simulator);
        try {
            SimulationContext context = new SimulationContextImpl(environment, getLibraries());
            return simulator.execute(context);
        } finally {
            history.offer(simulator);
            if (history.size() > properties.getHistorySize()) history.poll();
            running.remove(simulator);
        }
    }

    @Override
    public Resource getLog(int id) {
        RestResult result = findOutput(id);
        if (result.getLogsURI() != null) {
            return ResourceFactory.resolve(UriUtils.parseUri(result.getLogsURI())).withMimeType(MimeType.TEXT_PLAIN);
        } else {
            return MemoryResource.create("No logs are available");
        }
    }

    @Override
    public Resource getReport(int id) {
        RestResult result = findOutput(id);
        if (result.getLogsURI() != null) {
            return ResourceFactory.resolve(UriUtils.parseUri(result.getReportURI())).withMimeType(MimeType.TEXT_HTML);
        } else {
            return MemoryResource.create("No report are available");
        }
    }

    @Override
    public void reload() {
        RestCache cache = new RestCache();
        cache.setApplicationContext(applicationContext);
        cache.load();
        this.cache = cache;
        taskExecutor.execute(() -> projectManager.initialize(this));
        taskExecutor.execute(() -> simulationScheduler.initialize(this));
        taskExecutor.execute(() -> simulationScheduler.reload());
    }

    @Override
    public void afterPropertiesSet() {
        persistence.setApplicationContext(applicationContext);
        simulationScheduler.setApplicationContext(applicationContext);
        initializeProviders();
        initResources();
        registerHeimdall();
        this.reload();
        simulationScheduler.initialize(this);
    }

    RestProperties getProperties() {
        return properties;
    }

    Resource getProjectResource() {
        return projectResource;
    }

    private void initializeProviders() {
        LOGGER.debug("Discover simulation providers:");
        Collection<Simulation.Provider> simulationProviders = ClassUtils.resolveProviderInstances(Simulation.Provider.class);
        for (Simulation.Provider provider : simulationProviders) {
            LOGGER.debug(" - {}", ClassUtils.getName(provider));
            this.simulationProviders.add(provider);
        }
        LOGGER.info("Discovered {} simulation providers", simulationProviders.size());

        LOGGER.debug("Discover simulation executor providers:");
        Collection<Simulator.Provider> simulatorProviders = ClassUtils.resolveProviderInstances(Simulator.Provider.class);
        for (Simulator.Provider provider : simulatorProviders) {
            LOGGER.debug(" - {}", ClassUtils.getName(provider));
            this.simulatorProviders.add(provider);
        }
        LOGGER.info("Discovered {} simulator providers", simulatorProviders.size());
    }

    private void initResources() {
        Resource resource = resourceService.getShared("rest");
        initProjectResources();
        initScriptResources(resource);
        initLogResources(resource);
        initReportResources(resource);
    }

    private void registerHeimdall() {
        if (!properties.isSelf()) return;
        Project project = (Project) Project.create(UriUtils.parseUri("https://github.com/adrian-tarau/heimdall.git")).type(Project.Type.GIT)
                .libraryPath("/**/test/resources/rest/library/*.js").simulationPath("/**/test/resources/rest/simulation/*.js")
                .tag(SELF_TAG).tag(AUTO_TAG).tag(LOCAL_TAG)
                .name("Heimdall").description("A testing/monitoring tool for developers")
                .build();
        registerProject(project);
    }

    private void initScriptResources(Resource resource) {
        scriptResource = resource.resolve("script", Resource.Type.DIRECTORY);
        LOGGER.info("Rest scripts are stored in : " + scriptResource);
        if (scriptResource.isLocal()) {
            LOGGER.info("Rest scripts are stored in a RocksDB database: " + scriptResource);
            Resource dbScriptResource = RocksDbResource.create(scriptResource);
            try {
                dbScriptResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize snapshot store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("rest/script", dbScriptResource);
        } else {
            LOGGER.info("Rest scripts are stored in: " + scriptResource);
        }
    }

    private void initLogResources(Resource resource) {
        logsResource = resource.resolve("log", Resource.Type.DIRECTORY);
        if (logsResource.isLocal()) {
            LOGGER.info("Rest logs are stored in a RocksDB database: " + logsResource);
            Resource dbLogsResource = RocksDbResource.create(logsResource);
            try {
                dbLogsResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize statement store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("rest/log", dbLogsResource);
        } else {
            LOGGER.info("Rest logs are stored in: " + logsResource);
        }
    }

    private void initReportResources(Resource resource) {
        reportResource = resource.resolve("report", Resource.Type.DIRECTORY);
        if (reportResource.isLocal()) {
            LOGGER.info("Rest reports are stored in a RocksDB database: " + reportResource);
            Resource dbReportResource = RocksDbResource.create(reportResource);
            try {
                dbReportResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize statement store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("rest/report", dbReportResource);
        } else {
            LOGGER.info("Rest reports are stored in: " + reportResource);
        }
    }

    private void initProjectResources() {
        projectResource = resourceService.getPersisted("rest").resolve("project", Resource.Type.DIRECTORY);
        LOGGER.info("Rest projects are stored in: " + projectResource);
    }

    private Simulator createSimulator(Simulation simulation) {
        for (Simulator.Provider provider : simulatorProviders) {
            if (provider.supports(simulation)) {
                return provider.create(simulation);
            }
        }
        throw new SimulationException("A simulator cannot be provided for simulation: " + simulation.getName());
    }

    private Collection<Library> getLibraries(Simulation simulation) {
        Collection<Library> libraries = new ArrayList<>();
        for (Library library : cache.getLibraries()) {
            boolean enabledByType = library.getType() == simulation.getType();
            boolean enabledByProject = simulation.getProject() != null && simulation.getProject().equals(library.getProject());
            if (enabledByType && enabledByProject) {
                libraries.add(library);
            }
        }
        return libraries;
    }

    private RestResult findOutput(int id) {
        RestResult restResult = restResultRepository.findById(id).orElse(null);
        if (restResult == null) {
            throw new SimulationException("A simulation output with identifier " + id + " does not exist");
        }
        return restResult;
    }

}
