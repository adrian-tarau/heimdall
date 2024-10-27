package net.microfalx.heimdall.rest.core;

import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.Hashing;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.Resource;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

public class RestUtils {

    public static Metrics metrics = Metrics.of("Rest");

    /**
     * Returns the natural identifier for a resource.
     *
     * @param type     the simulation type
     * @param resource the resource
     * @return a non-null instance
     */
    public static String getNaturalId(Simulation.Type type, Resource resource) {
        requireNonNull(type);
        requireNonNull(resource);
        return type.name().toLowerCase() + "_" + Hashing.hash(toIdentifier(resource.getFileName()));
    }
}
