package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.lang.annotation.Provider;

import java.time.LocalDateTime;
import java.util.Collection;

@Provider
public class ScenarioDataSet extends MemoryDataSet<Scenario, PojoField<Scenario>, String> {

    public ScenarioDataSet(DataSetFactory<Scenario, PojoField<Scenario>, String> factory, Metadata<Scenario, PojoField<Scenario>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Scenario> extractModels(Filter filterable) {
        Collection<net.microfalx.heimdall.rest.api.Scenario> scenarios = getService(RestService.class).getScenarios();
        return scenarios.stream().map(this::from).toList();
    }

    private Scenario from(net.microfalx.heimdall.rest.api.Scenario scenario) {
        Scenario model = new Scenario();
        model.setId(scenario.getId());
        model.setSimulation(scenario.getSimulation());
        model.setScenario(scenario);
        model.setFirstExecutionAt(LocalDateTime.now());
        model.setLastExecutionAt(LocalDateTime.now());
        return model;
    }
}
