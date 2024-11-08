package net.microfalx.heimdall.rest.k6;

import net.microfalx.bootstrap.metrics.Metric;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractOutputParser;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVRecord;

public class K6OutputParser extends AbstractOutputParser {

    public K6OutputParser(AbstractSimulator simulator, Simulation simulation, SimulationContext simulationContext,
                          Resource resource) {
        super(simulator, simulation, simulationContext, resource);
    }

    @Override
    protected void process(CSVRecord record) {
        String scenario = record.get("scenario");
        Metric metric = getMetric(record.get("metric_name"));
        TimeSeries timeSeries = getTimeSeries(scenario, metric);
        long timestamp = Long.parseLong(record.get("timestamp"));
        timeSeries.add(Value.create(timestamp,
                Double.parseDouble(record.get("metric_value"))));
    }
}
