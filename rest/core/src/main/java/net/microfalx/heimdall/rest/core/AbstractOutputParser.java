package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for output parser.
 */
public abstract class AbstractOutputParser {

    protected final SimulationContext simulationContext;
    protected final Simulation simulation;
    private final Resource resource;

    protected final SimulationOutput output;

    public AbstractOutputParser(SimulationContext simulationContext, Simulation simulation, Resource resource) {
        requireNonNull(simulationContext);
        requireNonNull(simulation);
        requireNonNull(resource);
        this.simulationContext = simulationContext;
        this.simulation = simulation;
        this.resource = resource;
        this.output = new SimulationOutput(simulationContext.getEnvironment(), simulation);
    }

    /**
     * Parses the output and returns the simulation result as metrics.
     *
     * @return a non-null instance
     * @throws IOException if an I/O error occurs
     */
    public final Output parse() throws IOException {
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(resource.getReader());
        return output;
    }

    /**
     * Handles one record from the output file.
     *
     * @param record the record
     */
    protected abstract void handle(CSVRecord record);
}
