package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.metrics.Series;
import net.microfalx.heimdall.infrastructure.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;

/**
 * A service which manages pings.
 */
@Service
public class PingService implements InitializingBean, InfrastructureListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    @Autowired
    private PingPersistence persistence;

    @Autowired
    private PingCache cache;

    @Autowired
    private PingProperties properties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    private PingHealth health;

    private PingScheduler scheduler;
    private AsyncTaskExecutor taskExecutor;

    public Collection<net.microfalx.heimdall.infrastructure.ping.system.Ping> getRegisterPings() {
        return unmodifiableCollection(cache.getPings());
    }

    /**
     * Reload ping metadata from database.
     */
    public void reload() {
        cache.reload();
    }

    /**
     * Pings a service running on a server.
     *
     * @param service the service to ping
     * @param server  the server where the service runs
     * @return the result of the ping
     */
    public Ping ping(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        net.microfalx.heimdall.infrastructure.ping.system.Ping ping = cache.find(service, server);
        PingExecutor executor = new PingExecutor(ping, service, server, persistence, infrastructureService, health)
                .setPersist(false).setUseLiveness(true);
        return executor.execute();
    }

    /**
     * Returns a (time) series with the ping results for a given service and server.
     *
     * @param service the service
     * @param server  the server
     * @return a non-null series
     */
    public Series getSeries(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        return health.getSeries(service, server);
    }

    /**
     * Returns the number of statuses grouped by status.
     *
     * @param service the service
     * @param server  the server
     * @return a non-null map
     */
    public Map<Status, Long> getStatusCounts(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        return health.getStatusCounts(service, server);
    }

    /**
     * Registers a ping.
     * <p>
     * If a ping is already registered, the request is ignored.
     *
     * @param name        the name for the ping
     * @param description an optional description associated with a ping
     * @param service     the service
     * @param server      the server
     * @param interval    the interval
     * @return {@code true} if the ping was registered, {@code false} otherwise
     */
    public boolean registerPing(String name, String description,
                                net.microfalx.heimdall.infrastructure.api.Service service, Server server,
                                Duration interval) {
        boolean registered = persistence.registerPing(name, service, server, interval, description);
        if (registered) {
            LOGGER.info("Register ping '" + name + "' for service '" + service.getName() + "' and server '"
                    + service.getName() + "', interval " + interval);
        }
        return registered;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeExecutor();
        initializeScheduler();
    }

    @Override
    public void onInfrastructureEvent(InfrastructureEvent event) {
        provisionPings();
        reload();
    }

    @Override
    public Status getStatus(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        return health.getStatus(service, server);
    }

    @Override
    public Health getHealth(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        return health.getHealth(service, server);
    }

    @Override
    public void onInfrastructureInitialization() {
        provisionPings();
        reload();
        startScheduler();
    }

    AsyncTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    private void provisionPings() {
        this.taskExecutor.submit(new PingProvisioning(this, infrastructureService));
    }

    private void initializeExecutor() {
        AsynchronousProperties asynchronousProperties = new AsynchronousProperties();
        asynchronousProperties.setSuffix("ping");
        asynchronousProperties.setCoreThreads(properties.getThreads());
        this.taskExecutor = TaskExecutorFactory.create(asynchronousProperties).createExecutor();
        LOGGER.info("Ping services with " + properties.getThreads() + " threads");
    }

    private void initializeScheduler() {
        scheduler = new PingScheduler(cache, infrastructureService, persistence, health, taskExecutor);
    }

    private void startScheduler() {
        CronTrigger trigger = new CronTrigger("*/5 * * * * *");
        taskScheduler.schedule(scheduler, trigger);
    }
}
