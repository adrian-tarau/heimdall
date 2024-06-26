package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.infrastructure.api.Cluster;
import net.microfalx.heimdall.infrastructure.api.Dns;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.bootstrap.core.utils.HostnameUtils.isHostname;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Service
public class InfrastructureServiceImpl extends ApplicationContextSupport implements InfrastructureService, InitializingBean, ApplicationListener<ApplicationStartedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureService.class);

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    private volatile InfrastructureCache cache = new InfrastructureCache();
    private final InfrastructureHealth health = new InfrastructureHealth();
    private final InfrastructureDns dns = new InfrastructureDns();
    private final InfrastructurePersistence infrastructurePersistence = new InfrastructurePersistence();
    private final Collection<InfrastructureListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, Server> pendingServers = new ConcurrentHashMap<>();

    private volatile boolean started;

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
        doRegisterServer(server, null);
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
        Cluster existingCluster = cache.findByName(cluster.getName());
        if (existingCluster != null) cluster = mergeClusters(cluster, existingCluster);
        cache.registerCluster(cluster);
        infrastructurePersistence.execute(cluster);
        for (Server server : cluster.getServers()) {
            doRegisterServer(server, cluster);
        }
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
        cache.registerService(service);
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
        cache.registerEnvironment(environment);
        infrastructurePersistence.execute(environment);
    }

    @Override
    public Collection<Environment> find(Server server) {
        return cache.find(server);
    }

    @Override
    public Status getStatus(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        requireNonNull(service);
        requireNonNull(server);
        Status status = health.getStatus(service, server);
        if (health.shouldUpdateStatus(service, server)) {
            status = fireGetStatus(service, server);
            health.updateStatus(service, server, status);
        }
        return status;
    }

    @Override
    public Health getHealth(Environment environment) {
        return Health.NA;
    }

    @Override
    public Health getHealth(net.microfalx.heimdall.infrastructure.api.Service service) {
        return Health.NA;
    }

    @Override
    public Health getHealth(Server server) {
        return Health.NA;
    }

    @Override
    public Health getHealth(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        requireNonNull(service);
        requireNonNull(server);
        Health health = this.health.getHealth(service, server);
        if (this.health.shouldUpdateHealth(service, server)) {
            health = fireGetHealth(service, server);
            this.health.updateHealth(service, server, health);
        }
        return health;
    }

    @Override
    public Dns resolve(Server server) {
        return dns.getDns(server);
    }

    @Override
    public void reload() {
        InfrastructureCache cache = new InfrastructureCache();
        cache.setApplicationContext(getApplicationContext());
        cache.load();
        this.cache = cache;
        dns.load();
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        initializeListeners();
        taskExecutor.submit(this::fireInitializedEvent);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeApplicationContext();
        provisionInfrastructure();
        this.reload();
        started = true;
    }

    private void doRegisterServer(Server server, Cluster cluster) {
        server = dns.register(server);
        cache.registerServer(server);
        if (isHostname(server.getHostname())) {
            infrastructurePersistence.execute(server, cluster, null);
        } else {
            pendingServers.put(server.getId(), server);
        }
    }

    private void provisionInfrastructure() {
        taskExecutor.submit(new InfrastructureProvisioning(this));
    }

    private void initializeApplicationContext() {
        infrastructurePersistence.setApplicationContext(getApplicationContext());
        cache.setApplicationContext(getApplicationContext());
        dns.setApplicationContext(getApplicationContext());
        dns.setExecutor(taskExecutor);
    }

    private void initializeListeners() {
        LOGGER.info("Initialize listeners:");
        initializeListeners(ClassUtils.resolveProviderInstances(InfrastructureListener.class));
        initializeListeners(applicationContext.getBeansOfType(InfrastructureListener.class).values());
    }

    private void initializeListeners(Collection<InfrastructureListener> infrastructureListeners) {
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
            listener.onInfrastructureInitialization();
        }
    }

    private Status fireGetStatus(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        Status status = Status.NA;
        for (InfrastructureListener listener : listeners) {
            try {
                Status newStatus = listener.getStatus(service, server);
                if (newStatus != null && newStatus.isBefore(status)) status = newStatus;
            } catch (Exception e) {
                LOGGER.error("Failed to update status for service '" + service.getName() + "' running on server '" + server.getName() + "'", e);
            }
        }
        return status;
    }

    private Health fireGetHealth(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        Health health = Health.NA;
        for (InfrastructureListener listener : listeners) {
            try {
                Health newHealth = listener.getHealth(service, server);
                if (newHealth != null && newHealth.isBefore(health)) health = newHealth;
            } catch (Exception e) {
                LOGGER.error("Failed to update health for service '" + service.getName() + "' running on server '" + server.getName() + "'", e);
            }
        }
        return health;
    }

    private Cluster mergeClusters(Cluster source, Cluster target) {
        Map<String, Server> serversByIp = new HashMap<>();
        Cluster.Builder clusterBuilder = new Cluster.Builder(target.getId())
                .zoneId(target.getZoneId());
        target.getServers().forEach(server -> updateServer(serversByIp, server));
        source.getServers().forEach(server -> updateServer(serversByIp, server));
        serversByIp.values().forEach(clusterBuilder::server);
        clusterBuilder.tags(target.getTags()).tags(source.getTags())
                .name(target.getName()).description(target.getDescription());
        return clusterBuilder.build();
    }

    private void updateServer(Map<String, Server> serversByIp, Server server) {
        Dns dns = this.dns.getDns(server);
        server = this.dns.register(server);
        serversByIp.putIfAbsent(dns.getIp(), server);
        if (isHostname(server.getHostname())) serversByIp.put(dns.getIp(), server);
    }

}
