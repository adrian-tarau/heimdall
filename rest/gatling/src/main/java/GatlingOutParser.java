import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.heimdall.rest.core.AbstractOutputParser;
import net.microfalx.heimdall.rest.core.AbstractSimulator;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVRecord;

public class GatlingOutParser extends AbstractOutputParser {

    public GatlingOutParser(AbstractSimulator simulator, Simulation simulation, SimulationContext simulationContext, Resource resource) {
        super(simulator, simulation, simulationContext, resource);
    }

    @Override
    protected void process(CSVRecord record) {

    }
}
