package net.microfalx.heimdall.rest.k6;

import net.microfalx.bootstrap.metrics.Matrix;
import net.microfalx.bootstrap.metrics.Metric;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.rest.api.Metrics;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractOutputParser;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.heimdall.rest.core.SimulationOutput;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;

public class K6OutputParser extends AbstractOutputParser {

    private final Collection<Value> vus = new ArrayList<>();
    private final Collection<Value> vusMax = new ArrayList<>();

    public K6OutputParser(AbstractSimulator simulator, Simulation simulation, SimulationContext simulationContext,
                          Resource resource) {
        super(simulator, simulation, simulationContext, resource);
    }

    @Override
    protected void process(CSVRecord record) {
        String scenario = defaultIfEmpty(record.get("scenario"), "default");
        Metric metric = getMetric(record.get("metric_name"));
        int responseCode = getHttpStatusCode(record.get("code"));
        long timestamp = parseLong(record.get("timestamp"));
        Value value = Value.create(timestamp, parseDouble(record.get("metric_value")));
        if (Metrics.VUS.equals(metric)) {
            vus.add(value);
        } else if (Metrics.VUS_MAX.equals(metric)) {
            vusMax.add(value);
        } else {
            TimeSeries timeSeries = getTimeSeries(scenario, metric);
            timeSeries.add(value);
        }
    }

    @Override
    protected void completion(String scenario, SimulationOutput output) {
        super.completion(scenario, output);
        output.setVus(Matrix.create(Metrics.VUS, vus));
        output.setVusMax(Matrix.create(Metrics.VUS_MAX, vusMax));
    }
}
