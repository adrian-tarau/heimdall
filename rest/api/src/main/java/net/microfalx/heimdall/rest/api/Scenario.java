package net.microfalx.heimdall.rest.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A model for diverse workloads, or traffic patterns in load tests.
 */
@ToString
public class Scenario extends NamedAndTaggedIdentifyAware<String> {

    private Collection<Step> steps;
    private Duration startTime;
    private Duration gracefulStop;
    private String function;

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


    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private final Collection<Step> steps = new ArrayList<>();
        private Duration startTime = Duration.ZERO;
        private Duration gracefulStop = Duration.ofSeconds(30);
        private String function = "default";

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

        @Override
        protected IdentityAware<String> create() {
            return new Scenario();
        }

        @Override
        public Scenario build() {
            Scenario scenario = (Scenario) super.build();
            scenario.steps = steps;
            scenario.startTime = startTime;
            scenario.gracefulStop = gracefulStop;
            scenario.function = function;
            return scenario;
        }
    }
}
