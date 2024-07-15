package net.microfalx.heimdall.infrastructure.core.util;

import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.HealthAware;
import net.microfalx.heimdall.infrastructure.api.InfrastructureElement;
import net.microfalx.heimdall.infrastructure.core.InfrastructureProperties;
import net.microfalx.lang.TimeUtils;
import net.microfalx.lang.Timestampable;

import java.time.LocalDateTime;
import java.util.function.Function;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which inspects the health of infrastructure using a callback.
 *
 * @param <T> the type of infrastructure element
 */
public class HealthSummary<T extends InfrastructureElement> implements HealthAware, Timestampable<LocalDateTime> {

    private final Function<T, Health> extractor;
    private final long timestamp = System.currentTimeMillis();
    private int totalCount;
    private int unavailableCount = 0;
    private int degradedCount = 0;
    private int unhealthyCount = 0;

    private Health health;

    private InfrastructureProperties properties = new InfrastructureProperties();

    public HealthSummary(Function<T, Health> extractor) {
        requireNonNull(extractor);
        this.extractor = extractor;
    }

    /**
     * Changes the default properties (thresholds used to calculate health).
     *
     * @param properties the new properties
     * @return self
     */
    public HealthSummary<T> setProperties(InfrastructureProperties properties) {
        requireNonNull(properties);
        this.properties = properties;
        return this;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return TimeUtils.toLocalDateTime(timestamp);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getUnavailableCount() {
        return unavailableCount;
    }

    public int getUnhealthyCount() {
        return unhealthyCount;
    }

    public int getDegradedCount() {
        return degradedCount;
    }

    public Health getHealth() {
        if (totalCount == 0) return Health.NA;
        if (health != null) return health;
        if ((((float) unavailableCount / totalCount) * 100) >= properties.getUnavailableThreshold()) {
            health = Health.UNAVAILABLE;
        } else if ((((float) unhealthyCount / totalCount) * 100) >= properties.getUnhealthyThreshold()) {
            health = Health.UNHEALTHY;
        } else if ((((float) degradedCount / totalCount) * 100) >= properties.getDegradedThreshold()) {
            health = Health.DEGRADED;
        } else {
            health = Health.HEALTHY;
        }
        return health;
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
        health = null;
        totalCount++;
        Health health = extractor.apply(value);
        switch (health) {
            case UNAVAILABLE -> unavailableCount++;
            case DEGRADED -> degradedCount++;
            case HEALTHY -> unhealthyCount++;
        }
    }
}
