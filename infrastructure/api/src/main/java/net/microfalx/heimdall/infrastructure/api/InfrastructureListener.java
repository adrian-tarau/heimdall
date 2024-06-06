package net.microfalx.heimdall.infrastructure.api;

import org.atteo.classindex.IndexSubclasses;

/**
 * A listener for infrastructure changes
 */
@IndexSubclasses
public interface InfrastructureListener {

    /**
     * Invoked when an environment is added, changed or removed.
     */
    default void onEnvironmentChanged(Environment environment) {
        // empty by default
    }

    /**
     * Invoked when a cluster is added, changed or removed.
     */
    default void onClusterChanged(Cluster cluster) {
        // empty by default
    }

    /**
     * Invoked when a service is added, changed or removed.
     */
    default void onServiceChanged(Service service) {
        // empty by default
    }

    /**
     * Invoked when
     */
    default void onInitialization() {
        // empty by default
    }
}
