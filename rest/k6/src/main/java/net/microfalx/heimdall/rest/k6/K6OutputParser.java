package net.microfalx.heimdall.rest.k6;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractOutputParser;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVRecord;

public class K6OutputParser extends AbstractOutputParser {

    public K6OutputParser(SimulationContext simulationContext, Simulation simulation, Resource resource) {
        super(simulationContext, simulation, resource);
    }

    @Override
    protected void process(CSVRecord record) {

    }

    @Override
    protected void completion() {

    }
}
