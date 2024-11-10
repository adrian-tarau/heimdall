package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.metrics.Series;
import net.microfalx.heimdall.infrastructure.api.*;
import net.microfalx.heimdall.infrastructure.core.util.HealthSummary;
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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
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
    private TaskScheduler taskScheduler;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InfrastructureProperties properties;

    @Autowired
    private InfrastructureHealth health;

    private volatile InfrastructureCache cache = new InfrastructureCache();
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
        requireNonNull(server);
        return cache.find(server);
    }

    @Override
    public Collection<Server> getServers(net.microfalx.heimdall.infrastructure.api.Service service) {
        requireNonNull(service);
        return cache.getServers(service);
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
    public Health getHealth(InfrastructureElement element) {
        requireNonNull(element);
        return getHealthSummary(element).getHealth();
    }

    @Override
    public Collection<Health> getHealthTrend(InfrastructureElement element) {
        return health.getHealthTrend(element);
    }

    @Override
    public Series getHealthTrend(InfrastructureElement element, Health health) {
        return this.health.getHealthTrend(element, health);
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
        taskScheduler.schedule(new InfrastructureScheduler(this, health), new CronTrigger(properties.getSchedule()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeApplicationContext();
        provisionInfrastructure();
        this.reload();
        started = true;
    }

    public InfrastructureHealth getHealth() {
        return health;
    }

    @SuppressWarnings("unchecked")
    <T extends InfrastructureElement> HealthSummary<T> getHealthSummary(InfrastructureElement element) {
        if (element instanceof Environment) {
            return (HealthSummary<T>) getHealthSummary((Environment) element);
        } else if (element instanceof Cluster) {
            return (HealthSummary<T>) getHealthSummary((Cluster) element);
        } else if (element instanceof Server) {
            return (HealthSummary<T>) getHealthSummary((Server) element);
        } else if (element instanceof net.microfalx.heimdall.infrastructure.api.Service) {
            return (HealthSummary<T>) getHealthSummary((net.microfalx.heimdall.infrastructure.api.Service) element);
        } else {
            throw new IllegalStateException("Unhandled infrastructure element: " + ClassUtils.getName(element));
        }
    }

    private void doRegisterServer(Server server, Cluster cluster) {
        server = dns.register(server);
        cache.registerServer(server);
        if (isHostname(server.getHostname()) || Server.ID.equals(server.getId())) {
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
            try {
                listener.onInfrastructureInitialization();
            } catch (Exception e) {
                LOGGER.error("Failed to fire infrastructure initialization event for " + ClassUtils.getName(listener), e);
            }
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

    private HealthSummary<Server> getHealthSummary(Environment environment) {
        requireNonNull(environment);
        environment = cache.getEnvironment(environment.getId());
        HealthSummary<Server> clusterHealthSummary = new HealthSummary<Server>(this::getHealth).setProperties(properties);
        clusterHealthSummary.inspect(environment.getAllServers());
        return clusterHealthSummary;
    }

    private HealthSummary<Server> getHealthSummary(Cluster cluster) {
        requireNonNull(cluster);
        cluster = cache.getCluster(cluster.getId());
        HealthSummary<Server> serverHealthSummary = new HealthSummary<Server>(this::getHealth).setProperties(properties);
        serverHealthSummary.inspect(cluster.getServers());
        return serverHealthSummary;
    }

    private HealthSummary<Server> getHealthSummary(net.microfalx.heimdall.infrastructure.api.Service service) {
        requireNonNull(service);
        service = cache.getService(service.getId());
        HealthSummary<Server> serverHealthSummary = new HealthSummary<Server>(this::getHealth).setProperties(properties);
        serverHealthSummary.inspect(getServers(service));
        return serverHealthSummary;
    }

    private HealthSummary<net.microfalx.heimdall.infrastructure.api.Service> getHealthSummary(Server server) {
        requireNonNull(server);
        Server finalServer = cache.getServer(server.getId());
        HealthSummary<net.microfalx.heimdall.infrastructure.api.Service> healthSummary =
                new HealthSummary<net.microfalx.heimdall.infrastructure.api.Service>(service -> getHealth(service, finalServer))
                        .setProperties(properties);
        healthSummary.inspect(server.getServices());
        return healthSummary;
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
