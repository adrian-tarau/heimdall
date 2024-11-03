package net.microfalx.heimdall.rest.core;


import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.lang.ClassUtils;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

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

    private final RestPersistence persistence = new RestPersistence();
    private final Collection<Simulation.Provider> simulationProviders = new CopyOnWriteArrayList<>();
    private final Collection<Simulator.Provider> simulatorProviders = new CopyOnWriteArrayList<>();
    private Resource scriptResource;

    @Override
    public Collection<Project> getProjects() {
        return cache.getProjects();
    }

    @Override
    public Collection<Simulation> getSimulations() {
        return cache.getSimulations();
    }

    @Override
    public void registerSimulation(Simulation simulation) {
        cache.registerSimulation(simulation);
        persistence.save(simulation);
    }

    @Override
    public Collection<Schedule> getSchedules() {
        return cache.getSchedules();
    }

    @Override
    public Collection<Library> getLibraries() {
        return cache.getLibraries();
    }

    @Override
    public void registerLibrary(Library library) {
        cache.registerLibrary(library);
        persistence.save(library);
    }

    @Override
    public Resource registerResource(Resource resource) throws IOException {
        requireNonNull(resource);
        Resource target = scriptResource.resolve(resource.toHash());
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
    public void reload() {
        RestCache cache = new RestCache();
        cache.setApplicationContext(applicationContext);
        cache.load();
        this.cache = cache;
    }

    @Override
    public void afterPropertiesSet() {
        initializeProviders();
        initResources();
        persistence.setApplicationContext(applicationContext);
        this.reload();
    }

    private void initializeProviders() {
        LOGGER.info("Discover simulation providers:");
        Collection<Simulation.Provider> simulationProviders = ClassUtils.resolveProviderInstances(Simulation.Provider.class);
        for (Simulation.Provider provider : simulationProviders) {
            LOGGER.info(" - {}", ClassUtils.getName(provider));
            this.simulationProviders.add(provider);
        }
        LOGGER.info("Discover simulation executor providers:");
        Collection<Simulator.Provider> simulatorProviders = ClassUtils.resolveProviderInstances(Simulator.Provider.class);
        for (Simulator.Provider provider : simulatorProviders) {
            LOGGER.info(" - {}", ClassUtils.getName(provider));
            this.simulatorProviders.add(provider);
        }
    }

    private void initResources() {
        Resource resource = resourceService.getShared("rest");
        scriptResource = resource.resolve("script", Resource.Type.DIRECTORY);
        LOGGER.info("Rest scripts are stored in : " + scriptResource);
    }

}
