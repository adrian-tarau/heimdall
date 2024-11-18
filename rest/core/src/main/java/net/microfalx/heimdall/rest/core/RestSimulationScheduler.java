package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.heimdall.rest.api.Schedule;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Handles the simulation scheduling.
 */
class RestSimulationScheduler {

    private RestServiceImpl restService;
    private TaskScheduler scheduler;
    private TaskExecutor executor;
    private final Map<Schedule, ScheduledFuture<?>> schedules = new ConcurrentHashMap<>();
    private final Map<Schedule, Lock> locks = new ConcurrentHashMap<>();

    /**
     * Receives the required dependencies and creates the initial scheduler.
     *
     * @param restService the rest service
     */
    void initialize(RestServiceImpl restService) {
        this.restService = restService;
        this.createScheduler();
    }

    /**
     * Reloads the schedules from the database.
     */
    void reload() {

    }

    /**
     * Reloads a schedule.
     * <p>
     * The schedule is validated whether it already exists and if exists, scheduled to be canceled.
     *
     * @param schedule the schedule
     */
    void reload(Schedule schedule) {
        requireNonNull(schedule);
    }

    private void createScheduler() {
        RestProperties properties = restService.getProperties();
        AsynchronousProperties schedulerProperties = properties.getScheduler();
        executor = TaskExecutorFactory.create(schedulerProperties).createExecutor();
        schedulerProperties.setCoreThreads(schedulerProperties.getCoreThreads() / 4);
        schedulerProperties.setSuffix(schedulerProperties.getSuffix() + "_scheduler");
        scheduler = TaskExecutorFactory.create(schedulerProperties).createScheduler();
    }
}
