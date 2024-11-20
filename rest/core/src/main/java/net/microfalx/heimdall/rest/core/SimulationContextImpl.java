package net.microfalx.heimdall.rest.core;

import lombok.ToString;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.rest.api.Library;
import net.microfalx.heimdall.rest.api.SimulationContext;
import net.microfalx.lang.ArgumentUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@ToString
public class SimulationContextImpl implements SimulationContext {

    private final Environment environment;
    private final Collection<Library> libraries = new ArrayList<>();

    public SimulationContextImpl(Environment environment, Collection<Library> libraries) {
        ArgumentUtils.requireNonNull(environment);
        ArgumentUtils.requireNonNull(libraries);
        this.environment = environment;
        this.libraries.addAll(libraries);
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public Collection<Library> getLibraries() {
        return Collections.unmodifiableCollection(libraries);
    }
}
