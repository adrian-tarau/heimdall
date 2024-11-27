package net.microfalx.heimdall.rest.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;
import org.atteo.classindex.IndexSubclasses;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;

import static java.time.Duration.ofMinutes;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A simulation represents a collection of scenarios which VUs (virtual users) would execute to perform a given task.
 * <p>
 * The simulation holds a reference to the script which contains the actual simulation rules.
 */
@ToString
public class Simulation extends Library {

    private Duration timeout;
    private Collection<Scenario> scenarios;

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
        return (Builder) new Builder().resource(resource);
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
     * Returns the maximum execution time allowed for the simulation (default to 15 minutes).
     *
     * @return a non-null instance
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * An enum for a simulator.
     */
    public enum Type {

        /**
         * A tool to do load testing from <a href="https://grafana.com/docs/k6/latest/">Grafana</a>.
         */
        K6("text/javascript"),

        /**
         * A tool to do load testing from <a href="https://jmeter.apache.org/">Apache</a>.
         */
        JMETER("text/xml"),

        /**
         * A load testing tool which runs on <a href="https://docs.gatling.io/reference/script/core/simulation/">Java, Scala or Kotlin</a>.
         */
        GATLING("text/java");

        private final String mimeType;

        Type(String mimeType) {
            this.mimeType = mimeType;
        }

        /**
         * Returns the mime type associated with the simulation type
         *
         * @return a non-null instance
         */
        public String getMimeType() {
            return mimeType;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Type.class.getSimpleName() + "[", "]")
                    .add("name='" + name() + "'")
                    .add("mimeType='" + mimeType + "'")
                    .toString();
        }
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

    public static class Builder extends Library.Builder {

        private final Collection<Scenario> scenarios = new ArrayList<>();
        private Duration timeout = ofMinutes(15);

        public Builder() {
        }

        public Builder(String id) {
            super(id);
        }

        public Builder(Simulation simulation) {
            super(simulation);
            this.timeout(simulation.getTimeout());
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

        public Builder timeout(Duration duration) {
            requireNonNull(duration);
            this.timeout = duration;
            return this;
        }

        @Override
        public Simulation build() {
            Simulation simulation = (Simulation) super.build();
            simulation.scenarios = scenarios;
            simulation.timeout = timeout;
            return simulation;
        }
    }
}
