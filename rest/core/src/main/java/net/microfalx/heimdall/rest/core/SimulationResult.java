package net.microfalx.heimdall.rest.core;

import lombok.ToString;
import net.microfalx.heimdall.rest.api.*;
import net.microfalx.resource.Resource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@ToString(onlyExplicitlyIncluded = true)
public class SimulationResult implements Result {

    private final Simulator simulator;
    private final Collection<Output> outputs = new ArrayList<>();

    public SimulationResult(Simulator simulator, Collection<Output> outputs) {
        requireNonNull(simulator);
        requireNonNull(outputs);
        this.simulator = simulator;
        this.outputs.addAll(outputs);
    }

    @Override
    public LocalDateTime getStartTime() {
        return simulator.getStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return simulator.getEndTime();
    }

    @Override
    public Duration getDuration() {
        return simulator.getDuration();
    }

    @Override
    public Status getStatus() {
        return simulator.getStatus();
    }

    @Override
    public Simulation getSimulation() {
        return simulator.getSimulation();
    }

    @Override
    public Collection<Output> getOutputs() {
        return unmodifiableCollection(outputs);
    }

    @Override
    public Resource getReport() {
        return simulator.getReport();
    }

    @Override
    public Resource getLogs() {
        return simulator.getLogs();
    }
}
