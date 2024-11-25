package net.microfalx.heimdall.rest.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.heimdall.rest.api.SimulationContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@ToString
@Getter
@Setter
class SimulationContextImpl implements SimulationContext {

    private final Simulation simulation;
    private final Environment environment;
    private final Attributes<?> attributes;
    private final Collection<Library> libraries = new HashSet<>();

    private boolean manual;
    private String user;

    SimulationContextImpl(Environment environment, Simulation simulation, Collection<Library> libraries) {
        requireNonNull(environment);
        requireNonNull(simulation);
        requireNonNull(libraries);
        this.environment = environment;
        this.simulation = simulation;
        this.attributes = Attributes.create(environment.getAttributes(true));
        this.libraries.addAll(libraries);
    }

    @Override
    public Collection<Library> getLibraries() {
        return unmodifiableCollection(libraries);
    }

    public void addLibraries(Collection<Library> libraries) {
        requireNonNull(libraries);
        this.libraries.addAll(libraries);
    }

    public Optional<String> getUser() {
        return Optional.ofNullable(user);
    }
}
