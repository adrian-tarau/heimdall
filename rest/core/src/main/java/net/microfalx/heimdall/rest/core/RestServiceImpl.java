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
import net.microfalx.resource.*;
import net.microfalx.resource.archive.ArchiveUtils;
import net.microfalx.resource.rocksdb.RocksDbResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.heimdall.rest.api.RestConstants.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.NamedAndTaggedIdentifyAware.*;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.resource.Resource.FILE_NAME_ATTR;

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
    private final Map<String, LocalDateTime> lastScheduled = new ConcurrentHashMap<>();
    private Resource scriptResource;
    private Resource logsResource;
    private Resource reportResource;
    private Resource dataResource;
    private Resource projectResource;
    private Resource simulatorResource;
    private Resource simulationsResource;

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
        persistence.save(project);
        cache.registerProject(project, null);
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
        persistence.save(simulation);
        cache.registerSimulation(simulation, null);
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
    public Library getLibrary(String id) {
        requireNonNull(id);
        return cache.getLibrary(id);
    }

    @Override
    public void registerLibrary(Library library) {
        requireNonNull(library);
        persistence.save(library);
        cache.registerLibrary(library, null);
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
        } else if (resource.hasAttribute(DATA_ATTR)) {
            target = dataResource;
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
    public SimulationContext createContext(Environment environment, Simulation simulation) {
        return createContext(environment, simulation, Collections.emptyList());
    }

    @Override
    public SimulationContext createContext(Environment environment, Simulation simulation, Collection<Library> libraries) {
        requireNonNull(simulation);
        reload(simulation.getProject());
        try {
            simulation = getSimulation(simulation.getId());
        } catch (RestNotFoundException e) {
            // if not present in the database, use whatever is given
        }
        return new SimulationContextImpl(environment, simulation, libraries);
    }

    @Override
    public Result simulate(SimulationContext context) {
        requireNonNull(context);
        LOGGER.info("Execute simulation '{}' using environment '{}'", context.getSimulation().getName(), context.getEnvironment().getName());
        Simulator simulator = createSimulator(context.getSimulation());
        if (context instanceof SimulationContextImpl contextImpl) {
            contextImpl.addLibraries(getLibraries(context.getSimulation()));
        }
        String simulationKey = getKey(context.getSimulation(), context.getEnvironment());
        if (!context.isManual()) lastScheduled.put(simulationKey, LocalDateTime.now());
        running.add(simulator);
        try {
            return simulator.execute(context);
        } finally {
            history.offer(simulator);
            if (history.size() > properties.getHistorySize()) history.poll();
            running.remove(simulator);
        }
    }

    @Override
    public Future<Result> schedule(SimulationContext context) {
        requireNonNull(context);
        return simulationScheduler.schedule(context);
    }

    @Override
    public Optional<LocalDateTime> getLastRun(Simulation simulation, Environment environment) {
        requireNonNull(simulation);
        requireNonNull(environment);
        String simulationKey = getKey(simulation, environment);
        return Optional.ofNullable(lastScheduled.put(simulationKey, LocalDateTime.now()));
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
            Resource resource = ResourceFactory.resolve(UriUtils.parseUri(result.getReportURI()));
            return getLiveReport(resource, result.getEnvironment().getName(), result.getSimulation().getName());
        } else {
            return MemoryResource.create("No report are available");
        }
    }

    @Override
    public Resource getReport(Simulator simulator) {
        requireNonNull(simulator);
        return getLiveReport(simulator.getReport(), simulator.getEnvironment().getName(), simulator.getSimulation().getName());
    }

    @Override
    public Resource getData(int id) {
        RestResult result = findOutput(id);
        if (result.getLogsURI() != null) {
            return ResourceFactory.resolve(UriUtils.parseUri(result.getDataURI())).withMimeType(MimeType.TEXT_CSV);
        } else {
            return MemoryResource.create("No data is available");
        }
    }

    @Override
    public void reload() {
        reloadCache();
        taskExecutor.execute(() -> projectManager.initialize(this));
        taskExecutor.execute(() -> simulationScheduler.initialize(this));
        taskExecutor.execute(simulationScheduler::reload);
    }

    @Override
    public void reload(Project project) {
        requireNonNull(project);
        try {
            projectManager.reload(project);
            reloadCache();
        } catch (IOException e) {
            LOGGER.error("Failed to reload project '" + project.getName() + "'", e);
        }
    }

    @Override
    public void afterPropertiesSet() {
        persistence.setApplicationContext(applicationContext);
        simulationScheduler.setApplicationContext(applicationContext);
        initializeProviders();
        initResources();
        registerProjects();
        discoverGlobalLibraries();
        this.reload();
        simulationScheduler.initialize(this);
    }

    RestProperties getProperties() {
        return properties;
    }

    Resource getProjectResource() {
        return projectResource;
    }

    Resource getSimulatorResource() {
        return simulatorResource;
    }

    Resource getSimulationsResource() {
        return simulationsResource;
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
        initDataResources(resource);
    }

    private void registerProjects() {
        try {
            registerProject(Project.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("Failed to register default project", e);
        }
        try {
            registerProject(Project.GLOBAL);
        } catch (Exception e) {
            LOGGER.error("Failed to register default project", e);
        }
        if (!properties.isSelf()) return;
        Project project = (Project) Project.create(UriUtils.parseUri("https://github.com/adrian-tarau/heimdall.git")).type(Project.Type.GIT)
                .libraryPath("/**/test/resources/rest/library/*.js").simulationPath("/**/test/resources/rest/simulation/*.js")
                .tag(SELF_TAG).tag(AUTO_TAG).tag(LOCAL_TAG)
                .name("Heimdall").description("A testing/monitoring tool for developers")
                .build();
        try {
            registerProject(project);
        } catch (Exception e) {
            LOGGER.error("Failed to register Heimdall project", e);
        }
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

    private void initDataResources(Resource resource) {
        dataResource = resource.resolve("data", Resource.Type.DIRECTORY);
        if (dataResource.isLocal()) {
            LOGGER.info("Rest data is stored in a RocksDB database: " + dataResource);
            Resource dbDataResource = RocksDbResource.create(dataResource);
            try {
                dbDataResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize statement store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("rest/data", dbDataResource);
        } else {
            LOGGER.info("Rest data is stored in: " + dataResource);
        }
    }

    private void initProjectResources() {
        Resource persisted = resourceService.getPersisted("rest");
        projectResource = persisted.resolve("project", Resource.Type.DIRECTORY);
        LOGGER.info("Rest projects are stored in: {}", projectResource);
        simulatorResource = persisted.resolve("project", Resource.Type.DIRECTORY);
        LOGGER.info("Rest simulators are stored in: {}", simulatorResource);
        simulationsResource = persisted.resolve("project", Resource.Type.DIRECTORY);
        LOGGER.info("Rest simulations are stored in: {}", simulationsResource);
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
            boolean enabledByGlobal = library.isGlobal();
            boolean enabledByProject = simulation.getProject().equals(library.getProject());
            if (enabledByType && (enabledByGlobal || enabledByProject)) {
                libraries.add(library);
            }
        }
        return libraries;
    }

    private void reloadCache() {
        RestCache cache = new RestCache();
        cache.setApplicationContext(applicationContext);
        cache.load();
        this.cache = cache;
    }

    private RestResult findOutput(int id) {
        RestResult restResult = restResultRepository.findById(id).orElse(null);
        if (restResult == null) {
            throw new SimulationException("A simulation output with identifier " + id + " does not exist");
        }
        return restResult;
    }

    private Resource getLiveReport(Resource resource, String environment, String simulation) {
        boolean archive = false;
        boolean compressed = false;
        try {
            archive = ArchiveUtils.isArchived(resource);
            compressed = ArchiveUtils.isCompressed(resource);
        } catch (IOException e) {
            // ignore and consider is not an archive or compressed
        }
        if (archive || compressed) {
            String extension = compressed ? "gz" : "zip";
            String fileName = toIdentifier(environment + "_" + simulation) + "." + extension;
            resource = resource.withAttribute(FILE_NAME_ATTR, fileName);
        } else {
            resource = resource.withMimeType(MimeType.TEXT_HTML);
        }
        return resource;
    }

    private String getKey(Simulation simulation, Environment environment) {
        return environment.getId() + "_" + simulation.getId();
    }

    private void discoverGlobalLibraries() {
        try {
            ClassPathResource.directory("rest/global").walk((root, child) -> {
                if (child.isFile()) discoverGlobalLibrary(child);
                return true;
            });
        } catch (Exception e) {
            LOGGER.error("Failed to discover global libraries", e);
        }
    }

    private void discoverGlobalLibrary(Resource resource) {
        try {
            Simulation simulation = discover(resource);
            Resource storedResource = registerResource(resource.withAttribute(SCRIPT_ATTR, Boolean.TRUE));
            Library.Builder builder = new Library.Builder(resource.getId()).resource(storedResource).type(simulation.getType())
                    .project(Project.GLOBAL).path(resource.getPath()).global(Boolean.TRUE);
            builder.name(resource.getName());
            registerLibrary(builder.build());
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Failed to register global library {}", resource.toURI());
        }
    }
}
