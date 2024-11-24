package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;

import java.util.Collections;

@Provider
public class SimulationDataSet extends MemoryDataSet<Simulation, PojoField<Simulation>, String> {

    public SimulationDataSet(DataSetFactory<Simulation, PojoField<Simulation>, String> factory, Metadata<Simulation, PojoField<Simulation>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Simulation> extractModels() {
        return Collections.emptyList();
    }
}
