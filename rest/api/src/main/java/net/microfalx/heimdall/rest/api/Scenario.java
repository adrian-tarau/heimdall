package net.microfalx.heimdall.rest.api;

import lombok.ToString;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A model for diverse workloads, or traffic patterns in load tests.
 */
@ToString
public class Scenario extends NamedAndTaggedIdentifyAware<String> {

    private Simulation simulation;
    private Collection<Step> steps;
    private Duration startTime;
    private Duration gracefulStop;
    private String function;
    private Duration toleratingThreshold;
    private Duration frustratingThreshold;

    /**
     * Creates a scenario
     *
     * @param simulation the simulation which owns the scenario
     * @param name       the name of the scenario
     * @return a non-null instance
     */
    public static Builder create(Simulation simulation, String name) {
        return (Builder) new Builder().simulation(simulation).name(name);
    }

    /**
     * Returns the simulation which owns this scenario.
     *
     * @return a non-null instance
     */
    public Simulation getSimulation() {
        return simulation;
    }

    /**
     * Returns the steps executed for a scenario.
     *
     * @return a non-null instance
     */
    public Collection<Step> getSteps() {
        return steps;
    }

    /**
     * Return the time offset since the start of the test, at which point this scenario should begin execution.
     *
     * @return a non-null instance
     */
    public Duration getStartTime() {
        return startTime;
    }

    /**
     * Return the time to wait for iterations to finish executing before stopping them forcefully.
     *
     * @return a positive integer
     */
    public Duration getGracefulStop() {
        return gracefulStop;
    }

    /**
     * Returns the name of exported function to execute.
     *
     * @return a non-null instance
     */
    public String getFunction() {
        return function;
    }

    /**
     * The threshold that the users will tolerate the service
     *
     * @return a non-null instance
     */
    public Duration getToleratingThreshold() {
        return toleratingThreshold;
    }

    /**
     * The threshold that the users will be frustrated and stop using the service
     *
     * @return a non-null instance
     */
    public Duration getFrustratingThreshold() {
        return frustratingThreshold;
    }

    /**
     * Returns the natural identifier for a scenario name.
     *
     * @param simulation the simulation
     * @param name       the name of the scenario
     * @return a non-null instance
     */
    public static String getNaturalId(Simulation simulation, String name) {
        requireNonNull(simulation);
        requireNonNull(name);
        Hashing hashing = Hashing.create();
        hashing.update(simulation.getId());
        hashing.update(simulation.getProject().getId());
        hashing.update(toIdentifier(name));
        return hashing.asString();
    }


    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private Simulation simulation;
        private final Collection<Step> steps = new ArrayList<>();
        private Duration startTime = Duration.ZERO;
        private Duration gracefulStop = Duration.ofSeconds(30);
        private String function = "default";
        private Duration toleratingThreshold = Duration.ofMillis(500);
        private Duration frustratingThreshold = Duration.ofSeconds(2);

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder step(Step step) {
            steps.add(step);
            return this;
        }

        public Builder startTime(Duration startTime) {
            requireNonNull(startTime);
            this.startTime = startTime;
            return this;
        }

        public Builder gracefulStop(Duration gracefulStop) {
            requireNonNull(gracefulStop);
            this.gracefulStop = gracefulStop;
            return this;
        }

        public Builder function(String function) {
            requireNonNull(function);
            this.function = function;
            return this;
        }

        public Builder toleratingThreshold(Duration toleratingThreshold) {
            requireNonNull(toleratingThreshold);
            this.toleratingThreshold = toleratingThreshold;
            return this;
        }

        public Builder frustratingThreshold(Duration frustratingThreshold) {
            requireNonNull(frustratingThreshold);
            this.frustratingThreshold = frustratingThreshold;
            return this;
        }

        public Builder simulation(Simulation simulation) {
            requireNonNull(simulation);
            this.simulation = simulation;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Scenario();
        }

        @Override
        protected String updateId() {
            return getNaturalId(simulation, name());
        }

        @Override
        public Scenario build() {
            if (simulation == null) throw new IllegalArgumentException("Simulation is required");
            if (frustratingThreshold.compareTo(toleratingThreshold) < 0) {
                throw new IllegalArgumentException("Frustrating threshold (" + frustratingThreshold
                        + ") must be larger than tolerating threshold (" + toleratingThreshold + ")");
            }
            Scenario scenario = (Scenario) super.build();
            scenario.steps = steps;
            scenario.startTime = startTime;
            scenario.gracefulStop = gracefulStop;
            scenario.function = function;
            scenario.toleratingThreshold = toleratingThreshold;
            scenario.frustratingThreshold = frustratingThreshold;
            return scenario;
        }
    }
}
