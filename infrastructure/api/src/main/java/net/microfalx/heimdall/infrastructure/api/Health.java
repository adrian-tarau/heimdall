package net.microfalx.heimdall.infrastructure.api;

import net.microfalx.lang.annotation.Name;

import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An enum for the health of a service.
 */
public enum Health {

    /**
     * Indicates that the health check determined that the service was unhealthy, or an unhandled exception was
     * thrown while executing the health check.
     */
    UNHEALTHY(0),

    /**
     * Indicates that the health check determined that the service was in a degraded state.
     */
    DEGRADED(1),

    /**
     * Indicates that the health check determined that the service was healthy.
     */
    HEALTHY(2),

    /**
     * Indicates that the health check did not run.
     */
    @Name("N/A")
    NA(100);

    private int priority;

    Health(int priority) {
        this.priority = priority;
    }

    /**
     * Returns whether this health comes before another health regarding the severity of the health.
     *
     * @param health the other health
     * @return {@code true} if this status comes before, {@code false} otherwise
     */
    public boolean isBefore(Health health) {
        requireNonNull(health);
        return priority < health.priority;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Status.class.getSimpleName() + "[", "]")
                .add("priority=" + priority)
                .add("name=" + name())
                .toString();
    }
}
