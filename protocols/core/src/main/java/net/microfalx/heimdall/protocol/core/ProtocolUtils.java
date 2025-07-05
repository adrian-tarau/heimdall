package net.microfalx.heimdall.protocol.core;

import net.microfalx.metrics.Metrics;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Utility class for protocol-related operations.
 * <p>
 * This class provides methods to retrieve metrics for different protocol types.
 */
public class ProtocolUtils {

    private static final Metrics ROOT = Metrics.of("Protocol");

    /**
     * Returns the {@link Metrics metrics} for the protocol.
     *
     * @param type the event type
     * @return a non-null instance
     */
    public static Metrics getMetrics(Event.Type type) {
        requireNonNull(type);
        return ROOT.withGroup(type.name());
    }

    /**
     * Returns the {@link Metrics metrics} for the protocol to count event specific stats.
     *
     * @param type the event type
     * @return a non-null instance
     */
    public static Metrics getEventMetrics(Event.Type type) {
        return getMetrics(type).withGroup("Event");

    }
}
