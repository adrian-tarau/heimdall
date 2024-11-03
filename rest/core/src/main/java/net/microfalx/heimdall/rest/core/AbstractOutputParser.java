package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.metrics.Metric;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Base class for output parser.
 */
public abstract class AbstractOutputParser {

    private final SimulationContext simulationContext;
    private final Simulation simulation;
    private final Resource resource;

    private final Map<String, SimulationOutput> outputs = new HashMap<>();
    private final Map<String, Measurement> values = new HashMap<>();

    public AbstractOutputParser(SimulationContext simulationContext, Simulation simulation, Resource resource) {
        requireNonNull(simulationContext);
        requireNonNull(simulation);
        requireNonNull(resource);
        this.simulationContext = simulationContext;
        this.simulation = simulation;
        this.resource = resource;
    }

    public SimulationContext getSimulationContext() {
        return simulationContext;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    /**
     * Parses the output and returns the simulation result as metrics.
     *
     * @return a non-null instance
     * @throws IOException if an I/O error occurs
     */
    public final Collection<Output> parse() throws IOException {
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(resource.getReader());
        for (CSVRecord record : records) {
            process(record);
        }
        completion();
        return outputs.values().stream().map(output -> (Output) output).toList();
    }


    /**
     * Returns the output associated with a scenario.
     *
     * @param name the name of the scenario
     * @return a non-null instance
     */
    protected final SimulationOutput getOutput(String name) {
        requireNonNull(name);
        return outputs.computeIfAbsent(name.toLowerCase(),
                s -> new SimulationOutput(toIdentifier(name), name, simulationContext.getEnvironment(), simulation));
    }

    /**
     * Returns the collection which olds a list of values associated with a scenario and a metric.
     *
     * @param name   the name of the scenario
     * @param metric the metric
     * @return a non-null instance
     */
    protected final Measurement getValue(String name, Metric metric) {
        String id = toIdentifier(name) + "_" + metric.getId();
        return values.computeIfAbsent(id, s -> new Measurement(id, name, metric));
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
    protected abstract void completion();

    /**
     * Holds values for a scenario and a metric.
     */
    public static class Measurement implements Identifiable<String>, Nameable {

        private final String id;
        private final String name;
        private final Metric metric;
        private final Collection<Value> values = new ArrayList<>();

        Measurement(String id, String name, Metric metric) {
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

        public Collection<Value> getValues() {
            return values;
        }
    }
}
