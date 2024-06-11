package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.infrastructure.api.Cluster;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.*;
import net.microfalx.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Service
public class InfrastructureServiceImpl extends ApplicationContextSupport implements InfrastructureService, InitializingBean, ApplicationListener<ApplicationStartedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureService.class);

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    private volatile InfrastructureCache cache = new InfrastructureCache();
    private final InfrastructurePersistence infrastructurePersistence = new InfrastructurePersistence();
    private final Collection<InfrastructureListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public Collection<Server> getServers() {
        return unmodifiableCollection(cache.getServers().values());
    }

    @Override
    public Server getServer(String id) {
        return cache.getServer(id);
    }

    @Override
    public void registerServer(Server server) {
        requireNonNull(server);
        infrastructurePersistence.execute(server, null);
    }

    @Override
    public Collection<Cluster> getClusters() {
        return unmodifiableCollection(cache.getClusters().values());
    }

    @Override
    public Cluster getCluster(String id) {
        return cache.getCluster(id);
    }

    @Override
    public void registerCluster(Cluster cluster) {
        requireNonNull(cluster);
        infrastructurePersistence.execute(cluster);
    }

    @Override
    public Collection<net.microfalx.heimdall.infrastructure.api.Service> getServices() {
        return unmodifiableCollection(cache.getServices().values());
    }

    @Override
    public net.microfalx.heimdall.infrastructure.api.Service getService(String id) {
        return cache.getService(id);
    }

    @Override
    public void registerService(net.microfalx.heimdall.infrastructure.api.Service service) {
        requireNonNull(service);
        infrastructurePersistence.execute(service);
    }

    @Override
    public Collection<Environment> getEnvironments() {
        return unmodifiableCollection(cache.getEnvironments().values());
    }

    @Override
    public Environment getEnvironment(String id) {
        return cache.getEnvironment(id);
    }

    @Override
    public void registerEnvironment(Environment environment) {
        requireNonNull(environment);
        infrastructurePersistence.execute(environment);
    }

    @Override
    public Collection<Environment> find(Server server) {
        return cache.find(server);
    }

    @Override
    public void reload() {
        InfrastructureLoader infrastructureLoader = new InfrastructureLoader();
        this.cache = infrastructureLoader.load();
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        taskExecutor.submit(this::fireInitializedEvent);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        infrastructurePersistence.setApplicationContext(getApplicationContext());
        provisionInfrastructure();
        initializeListeners();
    }

    private void provisionInfrastructure() {
        taskExecutor.submit(() -> {
            new InfrastructureProvisioning(infrastructurePersistence).execute();
        });
    }

    private void initializeListeners() {
        LOGGER.info("Initialize listeners:");
        Collection<InfrastructureListener> infrastructureListeners = ClassUtils.resolveProviderInstances(InfrastructureListener.class);
        for (InfrastructureListener infrastructureListener : infrastructureListeners) {
            LOGGER.info(" - " + ClassUtils.getName(infrastructureListener));
            if (infrastructureListener instanceof ApplicationContextAware) {
                ((ApplicationContextAware) infrastructureListener).setApplicationContext(applicationContext);
            }
            this.listeners.add(infrastructureListener);
        }
    }

    private void fireInitializedEvent() {
        for (InfrastructureListener listener : listeners) {
            listener.onInitialization();
        }
    }

}
