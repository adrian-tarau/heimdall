package net.microfalx.heimdall.infrastructure.core.util;

import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.InfrastructureElement;
import net.microfalx.heimdall.infrastructure.core.InfrastructureProperties;

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

    private InfrastructureProperties properties = new InfrastructureProperties();

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

    public HealthSummary<T> setProperties(InfrastructureProperties properties) {
        requireNonNull(properties);
        this.properties = properties;
        return this;
    }

    public Health getHealth(){
        if (totalCount==0) return Health.NA;
        if (properties.getDegradedThreshold() >= degradedCount){
            return Health.DEGRADED;
        } else if (properties.getUnhealthyThreshold()>=unhealthyCount) {
           return Health.UNHEALTHY;
        } else if (properties.getUnavailableThreshold() >= unavailableCount) {
            return Health.UNAVAILABLE;
        }
        return Health.HEALTHY;
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
