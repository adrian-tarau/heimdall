package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;

@Provider
public class ClusterDataSet extends MemoryDataSet<Cluster, PojoField<Cluster>, String> {

    public ClusterDataSet(DataSetFactory<Cluster, PojoField<Cluster>, String> factory, Metadata<Cluster, PojoField<Cluster>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Cluster> extractModels() {
        return null;
    }
}
