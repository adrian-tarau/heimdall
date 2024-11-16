package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.heimdall.rest.api.Schedule;
import org.springframework.scheduling.TaskScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Handles the simulation scheduling.
 */
class RestSimulationScheduler {

    private RestServiceImpl restService;
    private TaskScheduler scheduler;
    private final Map<Schedule, ScheduledFuture<?>> schedules = new ConcurrentHashMap<>();

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
        scheduler = TaskExecutorFactory.create(properties.getScheduler()).createScheduler();
    }
}
