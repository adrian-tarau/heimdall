package net.microfalx.heimdall.protocol.core.simulator;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for protocol data set factories.
 */
public abstract class AbstractProtocolDataSetFactory<M, F extends Field<M>, ID> extends NamedAndTaggedIdentifyAware<String>
        implements ProtocolDataSetFactory<M, F, ID> {

    private final Resource resource;

    public AbstractProtocolDataSetFactory(Resource resource) {
        requireNonNull(resource);
        this.resource = resource;
    }

    /**
     * Returns the resource associated with this factory.
     *
     * @return the resource
     */
    public final Resource getResource() {
        return resource;
    }


}
