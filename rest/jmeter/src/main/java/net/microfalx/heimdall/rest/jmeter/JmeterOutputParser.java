package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.rest.api.Metrics;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractOutputParser;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVRecord;

class JmeterOutputParser extends AbstractOutputParser {

    public JmeterOutputParser(AbstractSimulator simulator, Simulation simulation, SimulationContext simulationContext,
                              Resource resource) {
        super(simulator, simulation, simulationContext, resource);
    }

    @Override
    protected void process(CSVRecord record) {
        String scenario = record.get("label");
        long timestamp =Long.parseLong(record.get("timeStamp"));

        TimeSeries timeSeries1 = getTimeSeries(scenario, Metrics.HTTP_REQ_DURATION);
        timeSeries1.add(Value.create(timestamp, Double.parseDouble(record.get("elapsed"))));

        TimeSeries timeSeries2 = getTimeSeries(scenario, Metrics.DATA_SENT);
        timeSeries2.add(Value.create(timestamp, Double.parseDouble(record.get("sentBytes"))));

        TimeSeries timeSeries3 = getTimeSeries(scenario, Metrics.VUS);
        timeSeries3.add(Value.create(timestamp, Double.parseDouble(record.get("grpThreads"))));

        TimeSeries timeSeries4 = getTimeSeries(scenario, Metrics.VUS_MAX);
        timeSeries4.add(Value.create(timestamp, Double.parseDouble(record.get("allThreads"))));

        TimeSeries timeSeries5 = getTimeSeries(scenario, Metrics.HTTP_REQ_CONNECTING);
        timeSeries5.add(Value.create(timestamp, Double.parseDouble(record.get("Connect"))));

        TimeSeries timeSeries6 = getTimeSeries(scenario, Metrics.DATA_RECEIVED);
        timeSeries6.add(Value.create(timestamp, Double.parseDouble(record.get("bytes"))));

    }
}
