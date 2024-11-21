package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.core.system.EnvironmentRepository;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Result;
import net.microfalx.heimdall.rest.api.Schedule;
import net.microfalx.heimdall.rest.core.system.RestResult;
import net.microfalx.heimdall.rest.core.system.RestResultRepository;
import net.microfalx.heimdall.rest.core.system.RestSimulationRepository;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.io.IOException;
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
class RestSimulationScheduler extends ApplicationContextSupport {

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

    private void persist(Schedule schedule, Result result) throws IOException {
        Resource resourceLogs = restService.registerResource(result.getLogs());
        Resource resourceReport = restService.registerResource(result.getReport());
        restService.registerResource(resourceReport);
        restService.registerResource(resourceLogs);
        RestResultRepository resultRepository = getBean(RestResultRepository.class);
        SimulationResult simulationResult = (SimulationResult) result;
        RestResult restResult= new RestResult();
        simulationResult.getOutputs().forEach(output -> {
            createRestResult(schedule, output, restResult, simulationResult);
            resultRepository.save(restResult);
        });
    }

    private void createRestResult(Schedule schedule, Output output, RestResult restResult, SimulationResult simulationResult) {
        restResult.setLogsURI(simulationResult.getLogs().toURI().toASCIIString());
        restResult.setReportURI(simulationResult.getReport().toURI().toASCIIString());
        restResult.setStatus(simulationResult.getStatus());
        Environment environment = getBean(InfrastructureService.class).getEnvironment(schedule.getEnvironment().getId());
        restResult.setEnvironment(getBean(EnvironmentRepository.class).findByNaturalId(environment.getId()).get());
        restResult.setSimulation(getBean(RestSimulationRepository.class).findByNaturalId(schedule.getId()).get());
        restResult.setVusMax((float) output.getVusMax().getAverage().getAsDouble());
        restResult.setVus((float) output.getVus().getAverage().getAsDouble());
        restResult.setDataReceived(output.getDataReceived().getValue().asFloat());
        restResult.setDuration(output.getDuration());
        restResult.setStartedAt(output.getStartTime());
        restResult.setEndedAt(output.getEndTime());
        restResult.setIterationDuration((float) output.getIterationDuration().getAverage().getAsDouble());
        restResult.setDataSent(output.getDataSent().getValue().asFloat());
        restResult.setIterations(output.getIterations().getValue().asFloat());
        restResult.setHttpRequestBlocked((float) output.getHttpRequestBlocked().getAverage().getAsDouble());
        restResult.setHttpRequests(output.getHttpRequests().getValue().asFloat());
        restResult.setHttpRequestConnecting((float) output.getHttpRequestConnecting().getAverage().getAsDouble());
        restResult.setHttpRequestFailed(output.getHttpRequestFailed().getValue().asFloat());
        restResult.setHttpRequestReceiving((float) output.getHttpRequestReceiving().getAverage().getAsDouble());
        restResult.setHttpRequestDuration((float) output.getHttpRequestDuration().getAverage().getAsDouble());
        restResult.setHttpRequestSending((float) output.getHttpRequestSending().getAverage().getAsDouble());
        restResult.setHttpRequestTlsHandshaking((float) output.getHttpRequestTlsHandshaking().getAverage().getAsDouble());
        restResult.setHttpRequestWaiting((float) output.getHttpRequestWaiting().getAverage().getAsDouble());
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
                    Result result = restService.simulate(schedule.getSimulation(), schedule.getEnvironment());
                    try {
                        persist(schedule, result);
                    } catch (Exception e) {
                        LOGGER.error(ExceptionUtils.getRootCauseDescription(e));
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
