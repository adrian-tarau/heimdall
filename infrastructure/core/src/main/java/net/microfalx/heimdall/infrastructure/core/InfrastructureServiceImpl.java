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
import java.util.HashMap;
import java.util.Map;
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

    private volatile Holder holder = new Holder();
    private final InfrastructureJpaManager infrastructureJpaManager = new InfrastructureJpaManager();
    private final Collection<InfrastructureListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public Collection<Server> getServers() {
        return unmodifiableCollection(holder.servers.values());
    }

    @Override
    public void registerServer(Server server) {
        requireNonNull(server);
        infrastructureJpaManager.execute(server, null);
    }

    @Override
    public Collection<Server> getClusters() {
        return unmodifiableCollection(holder.servers.values());
    }

    @Override
    public void registerCluster(Cluster cluster) {
        requireNonNull(cluster);
        infrastructureJpaManager.execute(cluster);
    }

    @Override
    public Collection<net.microfalx.heimdall.infrastructure.api.Service> getServices() {
        return unmodifiableCollection(holder.services.values());
    }

    @Override
    public void registerService(net.microfalx.heimdall.infrastructure.api.Service service) {
        requireNonNull(service);
        infrastructureJpaManager.execute(service);
    }

    @Override
    public Collection<Environment> getEnvironments() {
        return unmodifiableCollection(holder.environments.values());
    }

    @Override
    public void registerEnvironment(Environment environment) {
        requireNonNull(environment);
        infrastructureJpaManager.execute(environment);
    }

    @Override
    public void reload() {

    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        taskExecutor.submit(this::fireInitializedEvent);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        infrastructureJpaManager.setApplicationContext(getApplicationContext());
        provisionInfrastructure();
        initializeListeners();
    }

    private void provisionInfrastructure() {
        taskExecutor.submit(() -> {
            new InfrastructureProvisioning(infrastructureJpaManager).execute();
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

    private static class Holder {

        private final Map<String, Server> servers = new HashMap<>();
        private final Map<String, Cluster> clusters = new HashMap<>();
        private final Map<String, net.microfalx.heimdall.infrastructure.api.Service> services = new HashMap<>();
        private final Map<String, Environment> environments = new HashMap<>();
    }
}
