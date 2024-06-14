package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.FormatterUtils.formatDuration;
import static net.microfalx.lang.TimeUtils.millisSince;

/**
 * A class which handles scheduling pings.
 */
@Component
class PingScheduler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingScheduler.class);

    @Autowired
    private final PingCache cache;
    @Autowired
    private final InfrastructureService infrastructureService;
    @Autowired
    private final PingPersistence pingPersistence;
    @Autowired
    private final AsyncTaskExecutor taskExecutor;

    private final Map<Integer, LocalDateTime> lastScheduledTime = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicBoolean> pingRunning = new ConcurrentHashMap<>();

    PingScheduler(PingCache cache, AsyncTaskExecutor taskExecutor, InfrastructureService infrastructureService,
                  PingPersistence pingPersistence) {
        requireNonNull(cache);
        requireNonNull(taskExecutor);
        requireNonNull(infrastructureService);
        requireNonNull(pingPersistence);
        this.cache = cache;
        this.taskExecutor = taskExecutor;
        this.infrastructureService = infrastructureService;
        this.pingPersistence = pingPersistence;

    }

    @Override
    public void run() {
        LOGGER.info("Schedule pings");
        for (Ping ping : cache.getPings()) {
            LOGGER.info(" - " + ping.getName() + ", interval " + formatDuration(ping.getInterval()));
            schedule(ping);
        }
    }

    /**
     * Schedule a ping to be executed
     *
     * @param ping the ping to be schedule for running
     */
    private void schedule(Ping ping) {
        if (shouldBeScheduled(ping)) {
            Service service = infrastructureService.getService(ping.getService().getNaturalId());
            Server server = infrastructureService.getServer(ping.getServer().getNaturalId());
            PingExecutor pingExecutor = new PingExecutor(ping, service, server, pingPersistence, infrastructureService);
            lastScheduledTime.put(ping.getId(), LocalDateTime.now());
            taskExecutor.execute(new PingRunnable(pingExecutor));
        }
    }

    /**
     * See if the ping is currently running
     *
     * @param ping the ping to check to see if it is still running
     * @return @{code true} if the ping is currently running, otherwise @{code false}
     */
    private AtomicBoolean getRunning(Ping ping) {
        return pingRunning.computeIfAbsent(ping.getId(), v -> new AtomicBoolean());
    }

    /**
     * Check if the ping should be schedule.
     *
     * @param ping the ping to check if it is supposed to be schedule for running
     * @return false if the ping is not supposed to be schedule, otherwise true
     */
    private boolean shouldBeScheduled(Ping ping) {
        // check if already running
        AtomicBoolean running = getRunning(ping);
        if (running.get()) return false;
        // check if it is time to schedule again
        LocalDateTime lastScheduleTime = lastScheduledTime.get(ping.getId());
        if (lastScheduleTime == null) return true;
        return millisSince(lastScheduleTime) >= ping.getInterval();
    }

    /**
     * Wraps the ping to be submitted to the TaskExecutor
     */
    class PingRunnable implements Runnable {

        private final PingExecutor pingExecutor;

        public PingRunnable(PingExecutor pingExecutor) {
            this.pingExecutor = pingExecutor;
        }

        @Override
        public void run() {
            AtomicBoolean running = getRunning(pingExecutor.getPing());
            running.set(true);
            try {
                pingExecutor.execute();
            } finally {
                running.set(false);
            }
        }
    }
}
