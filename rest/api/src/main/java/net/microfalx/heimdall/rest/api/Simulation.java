package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.Hashing;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.resource.Resource;
import org.atteo.classindex.IndexSubclasses;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import static java.time.Duration.ofMinutes;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A simulation represents a collection of scenarios which VUs (virtual users) would execute to perform a given task.
 * <p>
 * The simulation holds a reference to the script which contains the actual simulation rules.
 */
public class Simulation extends NamedAndTaggedIdentifyAware<String> {

    private Collection<Scenario> scenarios;
    private Project project;
    private Type type;
    private Resource resource;
    private String path;
    private Duration timeout;

    /**
     * Creates a simulation builder out of a simulation identifier.
     *
     * @param id the simulation identifier
     * @return a non-null instance
     */
    public static Builder create(String id) {
        return new Builder(id);
    }

    /**
     * Creates a simulation builder out of a resource.
     *
     * @param resource the resource
     * @return a non-null instance
     */
    public static Builder create(Resource resource) {
        return new Builder().resource(resource);
    }

    /**
     * Returns the original path of the resource which supports this simulation.
     *
     * @return a non-null instance
     */
    public String getPath() {
        return path;
    }

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
     * @return a non-null instance
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Return the project repository
     *
     * @return a non-null instance
     */
    public Project getProject() {
        return project;
    }

    /**
     * Returns the maximum execution time allowed for the simulation (default to 15 minutes).
     *
     * @return a non-null instance
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Returns the natural identifier for a resource.
     *
     * @param type     the simulation type
     * @param resource the resource
     * @return a non-null instance
     */
    public static String getNaturalId(Type type, Resource resource) {
        requireNonNull(type);
        requireNonNull(resource);
        return type.name().toLowerCase() + "_" + Hashing.hash(toIdentifier(resource.getFileName()));
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
        private String path;
        private Project project;
        private Duration timeout = ofMinutes(15);

        public Builder(String id) {
            super(id);
        }

        public Builder(Simulation simulation) {
            super(simulation.getId());
            this.tags(simulation.getTags())
                    .name(simulation.getName())
                    .description(simulation.getDescription());
            this.type = simulation.getType();
            this.resource = simulation.getResource();
            this.project = simulation.getProject();
            this.timeout = simulation.getTimeout();
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
            if (emptyName()) this.name(resource.getName());
            if (isEmpty(path)) this.path = resource.getPath();
            return this;
        }

        public Builder path(String path) {
            requireNonNull(path);
            this.path = path;
            return this;
        }

        public Builder timeout(Duration duration) {
            requireNonNull(duration);
            this.timeout = duration;
            return this;
        }

        @Override
        protected String updateId() {
            if (resource != null) {
                return getNaturalId(type, resource);
            } else {
                return super.updateId();
            }
        }

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        @Override
        public Simulation build() {
            Simulation simulation = (Simulation) super.build();
            if (type == null) throw new IllegalArgumentException("Scenario type is required");
            if (resource == null) throw new IllegalArgumentException("Scenario script is required");
            simulation.scenarios = scenarios;
            simulation.type = type;
            simulation.timeout = timeout;
            simulation.resource = resource;
            simulation.project = project;
            simulation.path = path;
            return simulation;
        }
    }
}
