package net.microfalx.heimdall.rest.api;

import lombok.ToString;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import java.time.Duration;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A schedule for executing simulations on a schedule.
 */
@ToString
public class Schedule extends NamedAndTaggedIdentifyAware<String> {

    private Type type;
    private Environment environment;
    private Simulation simulation;

    private String expression;
    private Duration interval;

    private Duration duration;
    private Integer vus;
    private Integer iterations;
    private Attributes<?> attributes;

    /**
     * Returns the type of scheduling.
     *
     * @return a non-null enum
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the environment on which the simulation will be executed.
     *
     * @return a non-null instance
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Returns the simulation to be executed by this schedule
     *
     * @return a non-null instance
     */
    public Simulation getSimulation() {
        return simulation;
    }

    /**
     * Returns an optional duration to override the maximum execution time for a simulation.
     *
     * @return a non-null instance
     */
    public Optional<Duration> getDuration() {
        return ofNullable(duration);
    }

    /**
     * Returns an optional number of virtual users to override the number of users from simulation
     *
     * @return a non-null instance
     */
    public Optional<Integer> getVus() {
        return ofNullable(vus);
    }

    /**
     * Returns an optional number of iterations to override the number of iterations from simulation.
     *
     * @return a non-null instance
     */
    public Optional<Integer> getIterations() {
        return ofNullable(iterations);
    }

    /**
     * Returns the attributes associated with this schedule.
     *
     * @return a non-null instance
     */
    public Attributes<?> getAttributes(boolean all) {
        if (all) {
            Attributes<Attribute> allAttributes = Attributes.create(this.attributes);
            updateAttributes(allAttributes);
            return allAttributes;
        } else {
            return attributes;
        }
    }

    /**
     * Returns the scheduling expression (CRON) for type {@link Type#EXPRESSION}.
     *
     * @return a non-null instance
     */
    public String getExpression() {
        if (type != Type.EXPRESSION) {
            throw new IllegalArgumentException("Only a schedule of type 'EXPRESSION' has an expression");
        }
        return expression;
    }

    /**
     * Returns the scheduling interval for type {@link Type#INTERVAL}
     *
     * @return a non-null instance
     */
    public Duration getInterval() {
        if (type != Type.INTERVAL) {
            throw new IllegalArgumentException("Only a schedule of type 'INTERVAL' has an interval");
        }
        return interval;
    }

    private void updateAttributes(Attributes<?> attributes) {
        if (vus != null) attributes.addIfAbsent(RestConstants.VIRTUAL_USERS_ATTR, vus);
        if (duration != null) attributes.addIfAbsent(RestConstants.DURATION_ATTR, duration);
        if (iterations != null) attributes.addIfAbsent(RestConstants.DURATION_ATTR, iterations);
    }

    /**
     * An enum for the type of scheduling
     */
    public enum Type {

        /**
         * A Cron expression
         */
        EXPRESSION,

        /**
         * A time interval
         */
        INTERVAL,
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private Type type;
        private Environment environment;
        private Simulation simulation;

        private String expression;
        private Duration interval;

        private Duration duration;
        private Integer vus;
        private Integer iterations;

        private final Attributes<?> attributes = Attributes.create();

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder environment(Environment environment) {
            requireNonNull(environment);
            this.environment = environment;
            return this;
        }

        public Builder simulation(Simulation simulation) {
            requireNonNull(simulation);
            this.simulation = simulation;
            return this;
        }

        public Builder expression(String expression) {
            requireNonNull(expression);
            this.type = Type.EXPRESSION;
            this.expression = expression;
            return this;
        }

        public Builder interval(Duration interval) {
            requireNonNull(simulation);
            this.type = Type.INTERVAL;
            this.interval = interval;
            return this;
        }

        public Builder setDuration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder setVus(Integer vus) {
            this.vus = vus;
            return this;
        }

        public Builder setIterations(Integer iterations) {
            this.iterations = iterations;
            return this;
        }

        public Builder attribute(String name, Object value) {
            attributes.add(name, value);
            return this;
        }

        public Builder attributes(Attributes<?> attributes) {
            attributes.copyFrom(attributes);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Schedule();
        }

        @Override
        public Schedule build() {
            Schedule schedule = (Schedule) super.build();
            if (type == null) throw new IllegalArgumentException("Schedule type is required");
            if (environment == null) throw new IllegalArgumentException("Environment  is required");
            if (simulation == null) throw new IllegalArgumentException("Simulation  is required");
            schedule.type = type;
            schedule.environment = environment;
            schedule.simulation = simulation;
            schedule.expression = expression;
            schedule.interval = interval;
            schedule.attributes = attributes;
            return schedule;
        }
    }
}
