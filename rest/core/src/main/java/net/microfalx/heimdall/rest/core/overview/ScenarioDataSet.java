package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.lang.annotation.Provider;

import java.util.Collections;

@Provider
public class ScenarioDataSet extends MemoryDataSet<Scenario, PojoField<Scenario>, String> {

    public ScenarioDataSet(DataSetFactory<Scenario, PojoField<Scenario>, String> factory, Metadata<Scenario, PojoField<Scenario>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Scenario> extractModels() {
        return Collections.emptyList();
    }
}
