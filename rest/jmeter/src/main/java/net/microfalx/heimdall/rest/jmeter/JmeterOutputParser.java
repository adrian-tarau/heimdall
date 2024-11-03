package net.microfalx.heimdall.rest.jmeter;

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

    }

    @Override
    protected void completion() {

    }
}
