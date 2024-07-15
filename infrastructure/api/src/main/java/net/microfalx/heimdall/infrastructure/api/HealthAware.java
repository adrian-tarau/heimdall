package net.microfalx.heimdall.infrastructure.api;

/**
 * An interface implemented by infrastructure elements which can provide their health.
 */
public interface HealthAware {

    /**
     * Returns the health of this object.
     *
     * @return a non-null instance
     */
    Health getHealth();

    /**
     * Returns the total number of child infrastructure elements.
     *
     * @return a positive integer
     */
    int getTotalCount();

    /**
     * Returns the number of child infrastructure elements with their health equal to {@link Health#UNAVAILABLE}.
     *
     * @return a positive integer
     */
    int getUnavailableCount();

    /**
     * Returns the number of child infrastructure elements with their health equal to {@link Health#UNHEALTHY}.
     *
     * @return a positive integer
     */
    int getUnhealthyCount();

    /**
     * Returns the number of child infrastructure elements with their health equal to {@link Health#DEGRADED}.
     *
     * @return a positive integer
     */
    int getDegradedCount();


}
