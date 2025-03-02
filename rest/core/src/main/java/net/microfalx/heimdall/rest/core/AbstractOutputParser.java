package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.rest.api.*;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Matrix;
import net.microfalx.metrics.Metric;
import net.microfalx.metrics.Value;
import net.microfalx.metrics.Vector;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Base class for output parser.
 */
public abstract class AbstractOutputParser {

    private final AbstractSimulator simulator;
    private final SimulationContext simulationContext;
    private final Simulation simulation;
    private final Resource resource;

    private final Map<String, SimulationOutput> outputs = new HashMap<>();
    private final Map<String, TimeSeries> timeSeries = new HashMap<>();
    private final static Map<String, Metric> metrics = new HashMap<>();

    public AbstractOutputParser(AbstractSimulator simulator, Simulation simulation, SimulationContext simulationContext,
                                Resource resource) {
        requireNonNull(simulator);
        requireNonNull(simulation);
        requireNonNull(simulationContext);
        requireNonNull(resource);
        this.simulator = simulator;
        this.simulationContext = simulationContext;
        this.simulation = simulation;
        this.resource = resource;
    }

    /**
     * Parses the output and returns the simulation result as metrics.
     *
     * @return a non-null instance
     * @throws IOException if an I/O error occurs
     */
    public final Collection<Output> parse() throws IOException {
        CSVFormat format = CSVFormat.Builder.create(CSVFormat.RFC4180)
                .setHeader().setSkipHeaderRecord(true)
                .build();
        Iterable<CSVRecord> records = format.parse(resource.getReader());
        for (CSVRecord record : records) {
            process(record);
        }
        completion();
        return outputs.values().stream().map(output -> (Output) output).toList();
    }

    /**
     * Returns the time when the simulation was started.
     *
     * @return a non-null instance
     */
    protected final LocalDateTime getStartTime() {
        return simulator.getStartTime();
    }

    /**
     * Returns the time when the simulation has ended.
     *
     * @return a non-null instance
     */
    protected final LocalDateTime getEndTime() {
        return simulator.getEndTime();
    }

    /**
     * Returns the output associated with a scenario.
     *
     * @param name the name of the scenario
     * @return a non-null instance
     */
    protected final SimulationOutput getOutput(String name) {
        requireNonNull(name);
        Scenario scenario = simulator.getScenario(name);
        return outputs.computeIfAbsent(name.toLowerCase(),
                s -> new SimulationOutput(scenario, simulationContext.getEnvironment(), simulation));
    }

    /**
     * Returns a metric by name.
     *
     * @param name the name
     * @return a non-null instance
     */
    protected Metric getMetric(String name) {
        String identifier = toIdentifier(name);
        Metric metric = metrics.get(identifier);
        if (metric == null) metric = Metric.create(name);
        return metric;
    }

    /**
     * Returns the time-series for a scenario (its name) and a metric.
     *
     * @param name   the name of the scenario
     * @param metric the metric name
     * @return a non-null instance
     */
    protected final TimeSeries getTimeSeries(String name, Metric metric) {
        getOutput(name);
        String id = toIdentifier(name) + "_" + metric.getId();
        return timeSeries.computeIfAbsent(id, s -> new TimeSeries(id, name, metric));
    }

    /**
     * Returns all the scenarios.
     *
     * @return a non-null instance
     */
    protected final Collection<String> getScenarios() {
        return unmodifiableCollection(outputs.keySet());
    }

    /**
     * Invoked to process one record from the output file.
     *
     * @param record the record
     */
    protected abstract void process(CSVRecord record);

    /**
     * Invoked at the end of the output parsing to push all the metrics into the output.
     */
    protected void completion() {
        convertTimeSeriesToOutputs();
    }

    /**
     * Invoked at the end of processing for each scenario to fill in additional metrics.
     *
     * @param scenario the scenario
     * @param output   the output
     */
    protected void completion(String scenario, SimulationOutput output) {
        // empty by default
    }

    /**
     * Returns the HTTP error code base in a value received in the data.
     * <p>
     * If the value is empty, it is considered a success.
     * If the value cannot be parsed to an integer, it is considered a failure
     *
     * @param value the HTTP code as a string
     * @return the HTTP code
     */
    protected int getHttpStatusCode(String value) {
        if (StringUtils.isEmpty(value)) value = "200";
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 500;
        }
    }

    protected final void convertTimeSeriesToOutputs() {
        for (String scenario : getScenarios()) {
            SimulationOutput output = getOutput(scenario);
            output.setStartTime(getStartTime());
            output.setEndTime(getEndTime());

            output.setDataSent(getTimeSeries(scenario, Metrics.DATA_SENT).getVector(false));
            output.setDataReceived(getTimeSeries(scenario, Metrics.DATA_RECEIVED).getVector(false));
            output.setVus(getTimeSeries(scenario, Metrics.VUS).getMatrix());
            output.setVusMax(getTimeSeries(scenario, Metrics.VUS_MAX).getMatrix());
            output.setIterationDuration(getTimeSeries(scenario, Metrics.ITERATION_DURATION).getMatrix());
            output.setIterations(getTimeSeries(scenario, Metrics.ITERATIONS).getVector(false));

            output.setHttpRequests(getTimeSeries(scenario, Metrics.HTTP_REQS).getVector(false));
            output.setHttpRequestBlocked(getTimeSeries(scenario, Metrics.HTTP_REQ_BLOCKED).getMatrix());
            output.setHttpRequestConnecting(getTimeSeries(scenario, Metrics.HTTP_REQ_CONNECTING).getMatrix());
            output.setHttpRequestDuration(getTimeSeries(scenario, Metrics.HTTP_REQ_DURATION).getMatrix());
            output.setHttpRequestFailed(getTimeSeries(scenario, Metrics.HTTP_REQ_FAILED).getVector(false));
            output.setHttpRequestFailed4XX(getTimeSeries(scenario, Metrics.HTTP_REQ_FAILED_4XX).getVector(false));
            output.setHttpRequestFailed5XX(getTimeSeries(scenario, Metrics.HTTP_REQ_FAILED_5XX).getVector(false));
            output.setHttpRequestSending(getTimeSeries(scenario, Metrics.HTTP_REQ_SENDING).getMatrix());
            output.setHttpRequestTlsHandshaking(getTimeSeries(scenario, Metrics.HTTP_REQ_TLS_HANDSHAKING).getMatrix());
            output.setHttpRequestWaiting(getTimeSeries(scenario, Metrics.HTTP_REQ_WAITING).getMatrix());
            output.setHttpRequestReceiving(getTimeSeries(scenario, Metrics.DATA_RECEIVED).getMatrix());

            completion(scenario, output);
        }
    }

    private static void initMetrics() {
        for (Field field : Metrics.class.getFields()) {
            try {
                Metric metric = (Metric) field.get(null);
                metrics.put(toIdentifier(metric.getId()), metric);
            } catch (IllegalAccessException e) {
                // it should not happen
            }
        }
    }

    /**
     * A streams of timestamped values belonging to the same metric and scenario.
     */
    public static class TimeSeries implements Identifiable<String>, Nameable {

        private final String id;
        private final String name;
        private final Metric metric;
        private final Collection<Value> values = new ArrayList<>();

        TimeSeries(String id, String name, Metric metric) {
            this.id = id;
            this.name = name;
            this.metric = metric;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name + " / " + metric.getName();
        }

        public void add(Value value) {
            this.values.add(value);
        }

        public Matrix getMatrix() {
            return Matrix.create(metric, values);
        }

        public Vector getVector() {
            return getVector(true);
        }

        public Vector getVector(boolean average) {
            Matrix matrix = getMatrix();
            if (matrix.getValues().isEmpty()) {
                return Vector.empty(metric);
            } else {
                Value last = matrix.getLast().orElseThrow();
                return Vector.create(metric, Value.create(last.getTimestamp(), average ? matrix.getAverage().orElseThrow() : matrix.getSum()));
            }
        }

    }

    static {
        initMetrics();
    }
}
