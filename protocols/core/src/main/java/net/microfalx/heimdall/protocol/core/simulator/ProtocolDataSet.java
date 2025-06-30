package net.microfalx.heimdall.protocol.core.simulator;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;

import java.io.IOException;

/**
 * Base class for protocol data sets used in the simulator.
 * <p>
 * This class serves as a marker for different types of protocol data sets.
 */
public interface ProtocolDataSet<M, F extends Field<M>, ID> extends Identifiable<String>, Nameable, Iterable<M> {

    /**
     * Returns the content associated with this data set.
     *
     * @return a non-null
     */
    Resource getResource() throws IOException;

    /**
     * Returns the metadata associated with this data set.
     *
     * @return a non-null instance
     */
    Metadata<M, F, ID> getMetadata();

}
