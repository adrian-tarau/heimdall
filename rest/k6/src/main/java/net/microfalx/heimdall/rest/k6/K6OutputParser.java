package net.microfalx.heimdall.rest.k6;

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
            output.setDataReceived(getTimeSeries(scenario, Metrics.DATA_RECEIVED).getVector());
            output.setVus(getTimeSeries(scenario, Metrics.VUS).getVector());
            output.setVusMax(getTimeSeries(scenario, Metrics.VUS_MAX).getVector());
            output.setIterationDuration(getTimeSeries(scenario, Metrics.ITERATION_DURATION).getMatrix());
            output.setIterations(getTimeSeries(scenario, Metrics.ITERATIONS).getVector());

            output.setHttpRequests(getTimeSeries(scenario, Metrics.HTTP_REQS).getVector());
            output.setHttpRequestBlocked(getTimeSeries(scenario, Metrics.HTTP_REQ_BLOCKED).getMatrix());
            output.setHttpRequestConnecting(getTimeSeries(scenario, Metrics.HTTP_REQ_CONNECTING).getMatrix());
            output.setHttpRequestDuration(getTimeSeries(scenario, Metrics.HTTP_REQ_DURATION).getMatrix());
            output.setHttpRequestFailed(getTimeSeries(scenario, Metrics.HTTP_REQ_FAILED).getVector());
            output.setHttpRequestSending(getTimeSeries(scenario, Metrics.HTTP_REQ_SENDING).getMatrix());
            output.setHttpRequestTlsHandshaking(getTimeSeries(scenario, Metrics.HTTP_REQ_TLS_HANDSHAKING).getMatrix());
            output.setHttpRequestWaiting(getTimeSeries(scenario, Metrics.HTTP_REQ_WAITING).getMatrix());
            output.setHttpRequestReceiving(getTimeSeries(scenario, Metrics.DATA_RECEIVED).getMatrix());
        }
    }
}
