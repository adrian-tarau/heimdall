package net.microfalx.heimdall.rest.jmeter;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractOutputParser;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVRecord;

class JmeterOutputParser extends AbstractOutputParser {

    public JmeterOutputParser(SimulationContext simulationContext, Simulation simulation, Resource resource) {
        super(simulationContext, simulation, resource);
    }

    @Override
    protected void process(CSVRecord record) {

    }

    @Override
    protected void completion() {

    }
}
