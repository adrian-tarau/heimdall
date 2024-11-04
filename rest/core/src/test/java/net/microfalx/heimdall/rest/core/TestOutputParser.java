package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.metrics.Metric;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.rest.api.Metrics;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVRecord;

public class TestOutputParser extends AbstractOutputParser {

    public TestOutputParser(AbstractSimulator simulator, Simulation simulation, SimulationContext simulationContext, Resource resource) {
        super(simulator, simulation, simulationContext, resource);
    }

    @Override
    protected void process(CSVRecord record) {
        String scenario = record.get("scenario");
        Metric metric = getMetric(record.get("metric_name"));
        TimeSeries timeSeries = getTimeSeries(scenario, metric);
        timeSeries.add(Value.create(Long.parseLong(record.get("timestamp")),
                Double.parseDouble(record.get("metric_value"))));
    }

    @Override
    protected void completion() {
        for (String scenario : getScenarios()) {
            SimulationOutput output = getOutput(scenario);
            output.setStartTime(getStartTime());
            output.setEndTime(getEndTime());

            output.setDataSent(getTimeSeries(scenario, Metrics.DATA_SENT).getVector());
            output.setIterationDuration(getTimeSeries(scenario, Metrics.ITERATION_DURATION).getMatrix());
            output.setIterations(getTimeSeries(scenario, Metrics.ITERATIONS).getVector());

            output.setHttpRequests(getTimeSeries(scenario, Metrics.HTTP_REQS).getVector());
        }
    }
}
