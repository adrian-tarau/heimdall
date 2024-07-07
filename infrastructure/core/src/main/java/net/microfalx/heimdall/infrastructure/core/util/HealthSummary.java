package net.microfalx.heimdall.infrastructure.core.util;

import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.InfrastructureElement;

import java.util.function.Function;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which inspects the health of infrastructure using a callback.
 *
 * @param <T> the type of infrastructure element
 */
public class HealthSummary<T extends InfrastructureElement> {

    private final Function<T, Health> extractor;
    private int totalCount;
    private int unavailableCount = 0;
    private int degradedCount = 0;
    private int unhealthyCount = 0;

    public HealthSummary(Function<T, Health> extractor) {
        requireNonNull(extractor);
        this.extractor = extractor;
    }

    /**
     * Returns the total number of infrastructure elements.
     *
     * @return a positive integer
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Returns the number of infrastructure elements with their health equal to {@link Health#UNAVAILABLE}.
     *
     * @return a positive integer
     */
    public int getUnavailableCount() {
        return unavailableCount;
    }

    /**
     * Returns the number of infrastructure elements with their health equal to {@link Health#UNHEALTHY}.
     *
     * @return a positive integer
     */
    public int getUnhealthyCount() {
        return unhealthyCount;
    }

    /**
     * Returns the number of infrastructure elements with their health equal to {@link Health#DEGRADED}.
     *
     * @return a positive integer
     */
    public int getDegradedCount() {
        return degradedCount;
    }

    /**
     * Inspects a collection of infrastructure elements.
     *
     * @param values the infrastructure elements
     */
    public void inspect(Iterable<T> values) {
        requireNonNull(values);
        for (T value : values) {
            inspect(value);
        }
    }

    /**
     * Inspects a collection of infrastructure elements.
     *
     * @param value an infrastructure element
     */
    public void inspect(T value) {
        requireNonNull(value);
        totalCount++;
        Health health = extractor.apply(value);
        switch (health) {
            case UNAVAILABLE -> unavailableCount++;
            case DEGRADED -> degradedCount++;
            case HEALTHY -> unhealthyCount++;
        }
    }
}
