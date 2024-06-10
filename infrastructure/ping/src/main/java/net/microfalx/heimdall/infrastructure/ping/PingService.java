package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Ping;
import net.microfalx.heimdall.infrastructure.api.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

/**
 * A service which manages pings.
 */
@Service
public class PingService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    @Autowired
    private PingPersistence persistence;

    @Autowired
    private PingCache cache;

    @Autowired
    private PingScheduler scheduler;

    @Autowired
    private PingProperties properties;

    @Autowired
    private TaskScheduler taskScheduler;

    private AsyncTaskExecutor taskExecutor;

    @Autowired
    private InfrastructureService infrastructureService;

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
        PingExecutor executor = new PingExecutor(ping, service, server, persistence);
        return executor.execute();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reload();
        initializeExecutor();
        initializeScheduler();
    }

    private void initializeExecutor() {
        AsynchronousProperties asynchronousProperties = new AsynchronousProperties();
        asynchronousProperties.setSuffix("ping");
        asynchronousProperties.setCoreThreads(properties.getThreads());
        this.taskExecutor = TaskExecutorFactory.create(asynchronousProperties).createExecutor();
        LOGGER.info("Ping services with " + properties.getThreads() + " threads");
    }

    private void initializeScheduler() {
        CronTrigger trigger = new CronTrigger("*/5 * * * * *");
        taskScheduler.schedule(scheduler, trigger);
    }
}
