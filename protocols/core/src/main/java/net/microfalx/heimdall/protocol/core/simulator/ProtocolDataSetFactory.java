package net.microfalx.heimdall.protocol.core.simulator;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.util.Collection;

public interface ProtocolDataSetFactory<M, F extends Field<M>, ID> extends Identifiable<String>, Nameable {

    /**
     * Returns a collection of available data set factories.
     *
     * @return a non-null instance
     */
    static Collection<ProtocolDataSetFactory> getFactories() {
        return ClassUtils.resolveProviderInstances(ProtocolDataSetFactory.class);
    }

    /**
     * Creates a new data set.
     *
     * @return a non-null instance
     */
    ProtocolDataSet<M, F, ID> createDataSet();
}
