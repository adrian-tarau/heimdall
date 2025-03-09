package net.microfalx.heimdall.rest.core;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.core.async.AsynchronousProperties;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.infrastructure.core.system.EnvironmentRepository;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.heimdall.rest.core.system.*;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Aggregation;
import net.microfalx.metrics.Value;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;
import net.microfalx.threadpool.AbstractRunnable;
import net.microfalx.threadpool.ThreadPool;
import net.microfalx.threadpool.Trigger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.joinNames;
import static net.microfalx.lang.TextUtils.abbreviateMiddle;

/**
 * Handles the simulation scheduling.
 */
@Slf4j
class RestSimulationScheduler extends ApplicationContextSupport {

    private RestServiceImpl restService;
    private ThreadPool threadPool;
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
        if (schedule.isActive()) {
            ScheduleTask task = new ScheduleTask(schedule);
            if (schedule.getType() == Schedule.Type.EXPRESSION) {
                future = threadPool.schedule(task, Trigger.cron(schedule.getExpression()));
            } else {
                future = threadPool.schedule(task, Trigger.fixedRate(schedule.getInterval()));
            }
            schedules.put(schedule, future);
        }
    }

    /**
     * Schedules a simulation to run asynchronously.
     *
     * @param context the simulation context
     * @return the future
     */
    Future<Result> schedule(SimulationContext context) {
        requireNonNull(context);
        return threadPool.submit(new SimulationTask(context, null, false));
    }

    private void createScheduler() {
        RestProperties properties = restService.getProperties();
        AsynchronousProperties schedulerProperties = properties.getScheduler();
        if (threadPool == null) threadPool = ThreadPoolFactory.create(schedulerProperties).create();
    }

    private Lock getLock(Schedule schedule) {
        return locks.computeIfAbsent(schedule, s -> new ReentrantLock());
    }

    private void persist(SimulationContext context, Result result) throws IOException {
        Resource resourceLogs = restService.registerResource(result.getLogs());
        Resource resourceReport = restService.registerResource(result.getReport());
        Resource dataReport = restService.registerResource(result.getData());
        RestResult restResult = persistRestResult(context, result, resourceLogs, resourceReport, dataReport);
        result.getOutputs().forEach(output -> {
            RestScenario restScenario = persistRestScenario(output);
            persistRestOutput(restResult, restScenario, output);
        });
    }

    private void persistRestOutput(RestResult restResult, RestScenario restScenario, Output output) {
        RestOutputRepository restOutputRepository = getBean(RestOutputRepository.class);
        RestOutput restOutput = new RestOutput();
        restOutput.setResult(restResult);
        restOutput.setEnvironment(restResult.getEnvironment());
        restOutput.setSimulation(restResult.getSimulation());
        restOutput.setScenario(restScenario);
        restOutput.setStartedAt(output.getStartTime());
        restOutput.setEndedAt(output.getEndTime());
        restOutput.setDuration((int) output.getDuration().toMillis());
        restOutput.setApdex(output.getApdex());
        restOutput.setStatus(restResult.getStatus());

        restOutput.setVus((float) output.getVus().getAverage().orElse(0));
        restOutput.setVusMax((float) output.getVusMax().getAverage().orElse(0));
        restOutput.setIterations(output.getIterations().getValue().asFloat());
        restOutput.setIterationDuration((float) output.getIterationDuration().getAverage().orElse(0));
        restOutput.setDataReceived(output.getDataSent().getValue().asFloat());
        restOutput.setDataSent(output.getDataSent().getValue().asFloat());

        restOutput.setHttpRequestWaiting((float) output.getHttpRequestWaiting().getAverage().orElse(0));
        restOutput.setHttpRequests(output.getHttpRequests().getValue().asFloat());
        restOutput.setHttpRequestTlsHandshaking((float) output.getHttpRequestTlsHandshaking().getAverage().orElse(0));
        restOutput.setHttpRequestSending((float) output.getHttpRequestSending().getAverage().orElse(0));
        restOutput.setHttpRequestFailed(output.getHttpRequestFailed().getValue().asFloat());
        restOutput.setHttpRequestFailed4XX(output.getHttpRequestFailed4XX().getValue().asFloat());
        restOutput.setHttpRequestFailed5XX(output.getHttpRequestFailed5XX().getValue().asFloat());
        restOutput.setHttpRequestReceiving((float) output.getHttpRequestReceiving().getAverage().orElse(0));
        restOutput.setHttpRequestConnecting((float) output.getHttpRequestConnecting().getAverage().orElse(0));
        restOutput.setHttpRequestDuration((float) output.getHttpRequestDuration().getAverage().orElse(0));
        restOutput.setHttpRequestBlocked((float) output.getHttpRequestBlocked().getAverage().orElse(0));
        restOutputRepository.save(restOutput);
    }

    private RestScenario persistRestScenario(Output output) {
        restService.registerScenario(output.getScenario());
        RestScenarioRepository restScenarioRepository = getBean(RestScenarioRepository.class);
        return restScenarioRepository.findByNaturalId(output.getScenario().getId()).orElseThrow();
    }

    private RestResult persistRestResult(SimulationContext context, Result result, Resource resourceLogs, Resource resourceReport, Resource dataReport) {
        RestResult restResult = new RestResult();

        Optional<RestSimulation> jpaSimulation = getBean(RestSimulationRepository.class).findByNaturalId(result.getSimulation().getId());
        restResult.setSimulation(jpaSimulation.orElseThrow());
        Optional<net.microfalx.heimdall.infrastructure.core.system.Environment> jpaEnvironment = getBean(EnvironmentRepository.class).findByNaturalId(context.getEnvironment().getId());
        restResult.setEnvironment(jpaEnvironment.orElseThrow());
        restResult.setVersion(restResult.getEnvironment().getVersion());

        restResult.setStatus(result.getStatus());
        restResult.setErrorMessage(abbreviateMiddle(result.getErrorMessage(), 500));
        restResult.setLogsURI(ResourceUtils.toUri(resourceLogs));
        restResult.setReportURI(ResourceUtils.toUri(resourceReport));
        restResult.setDataURI(ResourceUtils.toUri(dataReport));
        restResult.setStartedAt(result.getStartTime());
        restResult.setEndedAt(result.getEndTime());
        restResult.setDuration((int) result.getDuration().toMillis());
        restResult.setApdex(result.getApdex());

        if (result.getStatus() == Status.SUCCESSFUL) {
            restResult.setVus(extractMetricFromMatrix(result, output -> output.getVus().getAverage()));
            restResult.setVusMax(extractMetricFromMatrix(result, output -> output.getVusMax().getAverage()));
            restResult.setIterations(extractMetricFromVector(result, output -> output.getIterations().getValue(), Aggregation.Type.SUM));
            restResult.setIterationDuration(extractMetricFromMatrix(result, output -> output.getIterationDuration().getAverage()));
            restResult.setDataSent(extractMetricFromVector(result, output -> output.getDataSent().getValue(), Aggregation.Type.SUM));
            restResult.setDataReceived(extractMetricFromVector(result, output -> output.getDataReceived().getValue(), Aggregation.Type.SUM));

            restResult.setHttpRequestSending(extractMetricFromMatrix(result, output -> output.getHttpRequestSending().getAverage()));
            restResult.setHttpRequestFailed(extractMetricFromVector(result, output -> output.getHttpRequestFailed().getValue(), Aggregation.Type.SUM));
            restResult.setHttpRequestFailed4XX(extractMetricFromVector(result,output -> output.getHttpRequestFailed4XX().getValue(), Aggregation.Type.SUM));
            restResult.setHttpRequestFailed5XX(extractMetricFromVector(result,output -> output.getHttpRequestFailed5XX().getValue(), Aggregation.Type.SUM));
            restResult.setHttpRequestDuration(extractMetricFromMatrix(result, output -> output.getHttpRequestDuration().getAverage()));
            restResult.setHttpRequestTlsHandshaking(extractMetricFromMatrix(result, output -> output.getHttpRequestTlsHandshaking().getAverage()));
            restResult.setHttpRequestReceiving(extractMetricFromMatrix(result, output -> output.getHttpRequestReceiving().getAverage()));
            restResult.setHttpRequestBlocked(extractMetricFromMatrix(result, output -> output.getHttpRequestBlocked().getAverage()));
            restResult.setHttpRequestWaiting(extractMetricFromMatrix(result, output -> output.getHttpRequestWaiting().getAverage()));
            restResult.setHttpRequests(extractMetricFromVector(result, output -> output.getHttpRequests().getValue()));
            restResult.setHttpRequestConnecting(extractMetricFromMatrix(result, output -> output.getHttpRequestConnecting().getAverage()));
        }

        RestResultRepository restResultRepository = getBean(RestResultRepository.class);
        restResultRepository.save(restResult);
        return restResult;
    }

    private float extractMetricFromVector(Result result, Function<Output, Value> function) {
        return extractMetricFromVector(result, function, Aggregation.Type.AVG);
    }

    private float extractMetricFromVector(Result result, Function<Output, Value> function, Aggregation.Type aggType) {
        Collection<Value> values = new ArrayList<>();
        for (Output output : result.getOutputs()) {
            values.add(function.apply(output));
        }
        DoubleStream stream = values.stream().mapToDouble(Value::asDouble);
        return switch (aggType) {
            case AVG -> (float) stream.average().orElse(0);
            case MIN -> (float) stream.min().orElse(0);
            case MAX -> (float) stream.max().orElse(0);
            case SUM -> (float) stream.sum();
        };
    }

    private float extractMetricFromMatrix(Result result, Function<Output, OptionalDouble> function) {
        return extractMetricFromMatrix(result, function, Aggregation.Type.AVG);
    }

    private float extractMetricFromMatrix(Result result, Function<Output, OptionalDouble> function, Aggregation.Type aggType) {
        Collection<Double> values = new ArrayList<>();
        for (Output output : result.getOutputs()) {
            values.add(function.apply(output).orElse(0));
        }
        DoubleStream stream = values.stream().mapToDouble(Double::doubleValue);
        return switch (aggType) {
            case AVG -> (float) stream.average().orElse(0);
            case MIN -> (float) stream.min().orElse(0);
            case MAX -> (float) stream.max().orElse(0);
            case SUM -> (float) stream.sum();
        };
    }

    class ScheduleTask extends AbstractRunnable {

        private final Schedule schedule;

        public ScheduleTask(Schedule schedule) {
            this.schedule = schedule;
            setName(joinNames("Rest", "Simulation", schedule.getName()));
        }

        @Override
        public void run() {
            SimulationContext context = restService.createContext(schedule.getEnvironment(), schedule.getSimulation());
            context.getAttributes().copyFrom(schedule.getAttributes(true));
            threadPool.submit(new SimulationTask(context, schedule, true));
        }
    }

    class SimulationTask implements Callable<Result> {

        private final SimulationContext context;
        private final Schedule schedule;
        private final boolean release;

        public SimulationTask(SimulationContext context, Schedule schedule, boolean release) {
            this.context = context;
            this.schedule = schedule;
            this.release = release;
        }

        @Override
        public Result call() throws Exception {
            Lock lock = schedule != null ? getLock(schedule) : null;
            if (lock != null && lock.tryLock()) {
                try {
                    return execute();
                } finally {
                    lock.unlock();
                }
            } else {
                return execute();
            }
        }

        private Result execute() {
            Result result;
            try {
                if (schedule != null) context.getAttributes().copyFrom(schedule.getAttributes(true));
                result = restService.simulate(context);
            } catch (Exception e) {
                throw new SimulationException(StringUtils.formatMessage("Failed to run simulation ''{0}'' using environment ''{1}''",
                        context.getSimulation().getName(), context.getEnvironment().getName(), e));
            }
            try {
                if (result != null) persist(context, result);
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to run simulation '{}' using environment '{}'",
                        context.getSimulation().getName(), context.getEnvironment().getName());
            }
            if (result != null && release) {
                try {
                    result.release();
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to cleanup simulation '{}' using environment '{}'",
                            context.getSimulation().getName(), context.getEnvironment().getName());
                }
            }
            return result;
        }
    }
}
