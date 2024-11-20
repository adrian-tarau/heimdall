package net.microfalx.heimdall.rest.core.overview;

import com.google.common.collect.Iterables;
import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.Simulator;
import net.microfalx.lang.annotation.Provider;

import java.time.Duration;
import java.util.stream.Collectors;

@Provider
public class SimulationRunningDataSet extends MemoryDataSet<SimulationRunning, PojoField<SimulationRunning>, String> {

    public SimulationRunningDataSet(DataSetFactory<SimulationRunning, PojoField<SimulationRunning>, String> factory, Metadata<SimulationRunning, PojoField<SimulationRunning>, String> metadata) {
        super(factory, metadata);
        setExpiration(Duration.ZERO);
    }

    @Override
    protected Iterable<SimulationRunning> extractModels() {
        RestService restService = getService(RestService.class);
        return Iterables.concat(restService.getRunning().stream().map(this::from).collect(Collectors.toList()),
                restService.getHistory().stream().map(this::from).collect(Collectors.toList()));
    }

    private SimulationRunning from(Simulator simulator) {
        Simulation simulation = simulator.getSimulation();
        SimulationRunning model = new SimulationRunning();
        model.setName(simulation.getName()).setDescription(simulation.getDescription()).setId(simulator.getId());
        model.setStartedAt(simulator.getStartTime()).setDuration(simulator.getDuration());
        model.setScript(simulation.getPath());
        return model;
    }
}
