package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

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
class PingScheduler implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingScheduler.class);

    private final PingCache cache;
    private final InfrastructureService infrastructureService;
    private final PingHealth health;
    private final PingPersistence pingPersistence;
    private final AsyncTaskExecutor taskExecutor;

    private final Map<Integer, LocalDateTime> lastScheduledTime = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicBoolean> pingRunning = new ConcurrentHashMap<>();

    PingScheduler(PingCache cache, InfrastructureService infrastructureService,
                  PingPersistence pingPersistence, PingHealth health, AsyncTaskExecutor taskExecutor) {
        requireNonNull(cache);
        requireNonNull(infrastructureService);
        requireNonNull(pingPersistence);
        requireNonNull(health);
        requireNonNull(taskExecutor);
        this.cache = cache;
        this.taskExecutor = taskExecutor;
        this.health = health;
        this.infrastructureService = infrastructureService;
        this.pingPersistence = pingPersistence;

    }

    @Override
    public void run() {
        LOGGER.debug("Schedule " + cache.getPings().size() + " pings");
        for (Ping ping : cache.getPings()) {
            LOGGER.debug(" - " + ping.getName() + ", interval " + formatDuration(ping.getInterval()));
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
            PingExecutor pingExecutor = new PingExecutor(ping, service, server, pingPersistence, infrastructureService, health);
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
