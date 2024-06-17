package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.heimdall.infrastructure.api.Ping;
import net.microfalx.heimdall.infrastructure.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;

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
    private ApplicationContext applicationContext;

    @Autowired
    private PingRepository pingRepository;

    private PingScheduler scheduler;
    private AsyncTaskExecutor taskExecutor;

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
        net.microfalx.heimdall.infrastructure.ping.Ping ping = cache.find(service, server);
        PingExecutor executor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        return executor.execute();
    }

    /**
     * Registers a ping.
     *
     * @param service  the service
     * @param server   the server
     * @param interval the interval
     */
    public void registerPing(net.microfalx.heimdall.infrastructure.api.Service service, Server server, Duration interval) {
        persistence.registerPing(service, server, interval);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeExecutor();
        initializeScheduler();
        reload();
    }

    @Override
    public void onInfrastructureEvent(InfrastructureEvent event) {
        provisionPings();
        reload();
    }

    @Override
    public void onInfrastructureInitialization() {
        provisionPings();
        startScheduler();
    }

    private void provisionPings() {
        this.taskExecutor.submit(new PingProvisioning(this, infrastructureService, pingRepository));
    }

    private void initializeExecutor() {
        AsynchronousProperties asynchronousProperties = new AsynchronousProperties();
        asynchronousProperties.setSuffix("ping");
        asynchronousProperties.setCoreThreads(properties.getThreads());
        this.taskExecutor = TaskExecutorFactory.create(asynchronousProperties).createExecutor();
        LOGGER.info("Ping services with " + properties.getThreads() + " threads");
    }

    private void initializeScheduler() {
        scheduler = new PingScheduler(cache, infrastructureService, persistence, taskExecutor);
    }

    private void startScheduler() {
        CronTrigger trigger = new CronTrigger("*/5 * * * * *");
        taskScheduler.schedule(scheduler, trigger);
    }
}
