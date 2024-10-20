package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.*;
import net.microfalx.resource.Resource;
import org.atteo.classindex.IndexSubclasses;

import java.util.ArrayList;
import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A simulation represents a collection of scenarios which VUs (virtual users) would execute to perform a given task.
 * <p>
 * The simulation holds a reference to the script which contains the actual simulation rules.
 */
public class Simulation extends NamedAndTaggedIdentifyAware<String> {

    private Collection<Scenario> scenarios;
    private Type type;
    private Resource resource;

    /**
     * Returns the scenarios part of the simulation.
     *
     * @return a non-null instance
     */
    public Collection<Scenario> getScenarios() {
        return scenarios;
    }

    /**
     * Returns the type of simulation (the tool which will execute the simulation).
     *
     * @return a non-null instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the script/file which contains the simulation plan.
     *
     * @return a non-null
     */
    public Resource getResource() {
        return resource;
    }

    public enum Type {

        /**
         * A tool to do load testing from <a href="https://grafana.com/docs/k6/latest/">Grafana</a>.
         */
        K6,

        /**
         * A tool to do load testing from <a href="https://jmeter.apache.org/">Apache</a>.
         */
        JMETER,

        /**
         * A load testing tool which runs on <a href="https://docs.gatling.io/reference/script/core/simulation/">Java, Scala or Kotlin</a>.
         */
        GATLING
    }

    /**
     * A provider which knows how to load a simulation from the script.
     */
    @IndexSubclasses
    public interface Provider extends Nameable {

        /**
         * Returns whether this provider can create a simulation out of a given script.
         *
         * @param resource the resource
         * @return {@code true} if a simulation can be created, {@code false} otherwise
         */
        boolean supports(Resource resource);

        /**
         * Creates the simulation out of the script.
         *
         * @param resource the resource
         * @return a non-null instance
         */
        Simulation create(Resource resource);
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private final Collection<Scenario> scenarios = new ArrayList<>();
        private Type type;
        private Resource resource;

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        @Override
        protected IdentityAware<String> create() {
            return new Simulation();
        }

        public Builder scenario(Scenario scenario) {
            requireNonNull(scenario);
            this.scenarios.add(scenario);
            return this;
        }

        public Builder type(Type type) {
            requireNonNull(type);
            this.type = type;
            return this;
        }

        public Builder resource(Resource resource) {
            requireNonNull(resource);
            this.resource = resource;
            this.id(Hashing.hash(StringUtils.toIdentifier(resource.getFileName())));
            return this;
        }

        @Override
        public Simulation build() {
            Simulation simulation = (Simulation) super.build();
            if (type == null) throw new IllegalArgumentException("Scenario type is required");
            if (resource == null) throw new IllegalArgumentException("Scenario script is required");
            simulation.scenarios = scenarios;
            simulation.type = type;
            simulation.resource = resource;
            return simulation;
        }
    }
}
