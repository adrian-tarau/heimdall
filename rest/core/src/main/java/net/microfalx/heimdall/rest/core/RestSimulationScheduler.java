package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Schedule;
import net.microfalx.lang.ExceptionUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Handles the simulation scheduling.
 */
@Slf4j
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
        for (Schedule schedule : restService.getSchedules()) {
            try {
                reload(schedule);
            } catch (Exception e) {
                LOGGER.warn(ExceptionUtils.getRootCauseDescription(e));
            }
        }
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
        ScheduledFuture<?> future = this.schedules.remove(schedule);
        if (future != null) future.cancel(false);
        ScheduleTask task = new ScheduleTask(schedule);
        if (schedule.getType() == Schedule.Type.EXPRESSION) {
            future = scheduler.schedule(task, new CronTrigger(schedule.getExpression()));
        } else {
            future = scheduler.schedule(task, new PeriodicTrigger(schedule.getInterval()));
        }
        schedules.put(schedule, future);
    }

    private void createScheduler() {
        RestProperties properties = restService.getProperties();
        AsynchronousProperties schedulerProperties = properties.getScheduler();
        if (executor == null) executor = TaskExecutorFactory.create(schedulerProperties).createExecutor();
        schedulerProperties.setCoreThreads(schedulerProperties.getCoreThreads() / 4);
        schedulerProperties.setSuffix(schedulerProperties.getSuffix() + "_scheduler");
        if (scheduler == null) scheduler = TaskExecutorFactory.create(schedulerProperties).createScheduler();
    }

    private Lock getLock(Schedule schedule) {
        return locks.computeIfAbsent(schedule, s -> new ReentrantLock());
    }

    private void persist(Schedule schedule, Collection<Output> outputs) {
    }


    class ScheduleTask implements Runnable {

        private final Schedule schedule;

        public ScheduleTask(Schedule schedule) {
            this.schedule = schedule;
        }

        @Override
        public void run() {
            executor.execute(new SimulationTask(schedule));
        }
    }

    class SimulationTask implements Runnable {

        private final Schedule schedule;

        public SimulationTask(Schedule schedule) {
            this.schedule = schedule;
        }

        @Override
        public void run() {
            Lock lock = getLock(schedule);
            if (lock.tryLock()) {
                try {
                    Collection<Output> outputs = restService.simulate(schedule.getSimulation(), schedule.getEnvironment());
                    persist(schedule, outputs);
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
