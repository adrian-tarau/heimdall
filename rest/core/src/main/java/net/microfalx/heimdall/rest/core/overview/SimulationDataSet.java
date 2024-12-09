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
public class SimulationDataSet extends MemoryDataSet<Simulation, PojoField<Simulation>, String> {

    public SimulationDataSet(DataSetFactory<Simulation, PojoField<Simulation>, String> factory, Metadata<Simulation, PojoField<Simulation>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Simulation> extractModels(Filter filterable) {
        Collection<net.microfalx.heimdall.rest.api.Simulation> simulations = getService(RestService.class).getSimulations();
        return simulations.stream().map(this::from).toList();
    }

    private Simulation from(net.microfalx.heimdall.rest.api.Simulation simulation) {
        Simulation model = new Simulation();
        model.setId(simulation.getId());
        model.setSimulation(simulation);
        model.setFirstExecutionAt(LocalDateTime.now());
        model.setLastExecutionAt(LocalDateTime.now());
        return model;
    }
}
