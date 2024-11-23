package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.infrastructure.core.system.EnvironmentRepository;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Result;
import net.microfalx.heimdall.rest.api.Schedule;
import net.microfalx.heimdall.rest.core.system.*;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

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
        RestResult restResult = persistRestResult(schedule, result, resourceLogs, resourceReport);
        result.getOutputs().forEach(output -> {
            RestScenario restScenario = persistRestScenario(output, restResult);
            persistRestOutput(restResult, restScenario, output);
        });
    }

    private void persistRestOutput(RestResult restResult, RestScenario restScenario, Output output) {
        RestOutputRepository restOutputRepository = getBean(RestOutputRepository.class);
        RestOutput restOutput = new RestOutput();
        restOutput.setResult(restResult);
        restOutput.setEnvironment(restResult.getEnvironment());
        restOutput.setSimulation(restResult.getSimulation());
        restOutput.setDataReceived(output.getDataSent().getValue().asFloat());
        restOutput.setScenario(restScenario);
        restOutput.setDuration(output.getDuration());
        restOutput.setDataSent(output.getDataSent().getValue().asFloat());
        restOutput.setVus((float) output.getVus().getAverage().getAsDouble());
        restOutput.setVusMax((float) output.getVusMax().getAverage().getAsDouble());
        restOutput.setIterations(output.getIterations().getValue().asFloat());
        restOutput.setIterationDuration((float) output.getIterationDuration().getAverage().getAsDouble());
        restOutput.setDataReceived(output.getDataReceived().getValue().asFloat());
        restOutput.setEndedAt(output.getEndTime());
        restOutput.setStatus(restResult.getStatus());
        restOutput.setStartedAt(output.getStartTime());
        restOutput.setHttpRequestWaiting((float) output.getHttpRequestWaiting().getAverage().getAsDouble());
        restOutput.setHttpRequests(output.getHttpRequests().getValue().asFloat());
        restOutput.setHttpRequestTlsHandshaking((float) output.getHttpRequestTlsHandshaking().getAverage().getAsDouble());
        restOutput.setHttpRequestSending((float) output.getHttpRequestSending().getAverage().getAsDouble());
        restOutput.setHttpRequestFailed(output.getHttpRequestFailed().getValue().asFloat());
        restOutput.setHttpRequestReceiving((float) output.getHttpRequestReceiving().getAverage().getAsDouble());
        restOutput.setHttpRequestConnecting((float) output.getHttpRequestConnecting().getAverage().getAsDouble());
        restOutput.setHttpRequestDuration((float) output.getHttpRequestDuration().getAverage().getAsDouble());
        restOutput.setHttpRequestBlocked((float) output.getHttpRequestBlocked().getAverage().getAsDouble());
        restOutputRepository.save(restOutput);
    }

    private RestScenario persistRestScenario(Output output, RestResult restResult) {
        RestScenarioRepository restScenarioRepository = getBean(RestScenarioRepository.class);
        RestScenario restScenario = new RestScenario();
        restScenario.setNaturalId(output.getId());
        restScenario.setName(output.getName());
        restScenario.setSimulation(restResult.getSimulation());
        restScenarioRepository.save(restScenario);
        return restScenario;
    }

    private RestResult persistRestResult(Schedule schedule, Result result, Resource resourceLogs, Resource resourceReport) {
        RestResult restResult = new RestResult();
        restResult.setStatus(result.getStatus());
        restResult.setLogsURI(resourceLogs.toURI().toASCIIString());
        restResult.setReportURI(resourceReport.toURI().toASCIIString());
        restResult.setHttpRequestSending(extractMetricFromMatrix(result, output -> output.getHttpRequestSending().getAverage()));
        restResult.setHttpRequestFailed(extractMetricFromVector(result, output -> output.getHttpRequestFailed().getValue()));
        restResult.setHttpRequestDuration(extractMetricFromMatrix(result, output -> output.getHttpRequestDuration().getAverage()));
        restResult.setHttpRequestTlsHandshaking(extractMetricFromMatrix(result, output -> output.getHttpRequestTlsHandshaking().getAverage()));
        restResult.setHttpRequestReceiving(extractMetricFromMatrix(result, output -> output.getHttpRequestReceiving().getAverage()));
        restResult.setHttpRequestBlocked(extractMetricFromMatrix(result, output -> output.getHttpRequestBlocked().getAverage()));
        restResult.setHttpRequestWaiting(extractMetricFromMatrix(result, output -> output.getHttpRequestWaiting().getAverage()));
        restResult.setHttpRequests(extractMetricFromVector(result, output -> output.getHttpRequests().getValue()));
        restResult.setIterations(extractMetricFromVector(result, output -> output.getIterations().getValue()));
        restResult.setIterationDuration(extractMetricFromMatrix(result, output -> output.getIterationDuration().getAverage()));
        restResult.setDataSent(extractMetricFromVector(result, output -> output.getDataSent().getValue()));
        restResult.setEndedAt(result.getEndTime());
        restResult.setDataReceived(extractMetricFromVector(result, output -> output.getDataReceived().getValue()));
        restResult.setDuration(result.getDuration());
        restResult.setVus(extractMetricFromMatrix(result, output -> output.getVus().getAverage()));
        restResult.setVusMax(extractMetricFromMatrix(result, output -> output.getVusMax().getAverage()));
        restResult.setHttpRequestConnecting(extractMetricFromMatrix(result, output -> output.getHttpRequestConnecting().getAverage()));
        restResult.setStartedAt(result.getStartTime());
        Optional<RestSimulation> jpaSimulation = getBean(RestSimulationRepository.class)
                .findByNaturalId(result.getSimulation().getId());
        restResult.setSimulation(jpaSimulation.get());
        net.microfalx.heimdall.infrastructure.core.system.Environment jpaEnvironment = getBean(EnvironmentRepository.class).findByNaturalId(schedule.getEnvironment().getId()).get();
        restResult.setEnvironment(jpaEnvironment);
        RestResultRepository restResultRepository = getBean(RestResultRepository.class);
        restResultRepository.save(restResult);
        return restResult;
    }

    private float extractMetricFromVector(Result result, Function<Output, Value> function) {
        Collection<Value> values = new ArrayList<>();
        for (Output output : result.getOutputs()) {
            values.add(function.apply(output));
        }
        return (float) values.stream().mapToDouble(Value::asDouble).average().orElse(0);
    }

    private float extractMetricFromMatrix(Result result, Function<Output, OptionalDouble> function) {
        Collection<Double> values = new ArrayList<>();
        for (Output output : result.getOutputs()) {
            values.add(function.apply(output).orElse(0));
        }
        return (float) values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
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
                    Result result = null;
                    try {
                        result = restService.simulate(schedule.getSimulation(), schedule.getEnvironment());
                    } catch (Exception e) {
                        LOGGER.error("Failed to run simulation '{}' on environment '{}'",
                                schedule.getSimulation().getName(), schedule.getEnvironment().getName(), e);
                    }
                    try {
                        if (result != null) persist(schedule, result);
                    } catch (IOException e) {
                        LOGGER.error("Failed to run simulation '{}' on environment '{}'",
                                schedule.getSimulation().getName(), schedule.getEnvironment().getName(), e);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
