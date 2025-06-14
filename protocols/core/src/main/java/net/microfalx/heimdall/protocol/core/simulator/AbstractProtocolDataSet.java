package net.microfalx.heimdall.protocol.core.simulator;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for protocol data sets.
 *
 * @param <M>  the model type
 * @param <F>  the field type
 * @param <ID> the identifier type
 */
public abstract class AbstractProtocolDataSet<M, F extends Field<M>, ID> extends NamedAndTaggedIdentifyAware<String>
        implements ProtocolDataSet<M, F, ID> {

    private final Resource resource;

    public AbstractProtocolDataSet(Resource resource) {
        requireNonNull(resource);
        this.resource = resource;
        setId(resource.getId());
        setName(resource.getName());
        setDescription(resource.getDescription());
    }

    @Override
    public final Resource getResource() {
        return resource;
    }
}
