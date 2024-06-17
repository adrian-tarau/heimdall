package net.microfalx.heimdall.infrastructure.api;

import org.atteo.classindex.IndexSubclasses;

/**
 * A listener for infrastructure changes
 */
@IndexSubclasses
public interface InfrastructureListener {

    /**
     * Invoked when an infrastructure element is added, changed or removed.
     */
    default void onInfrastructureEvent(InfrastructureEvent event) {
        // empty by default
    }

    /**
     * Invoked when the infrastructure was initialized.
     */
    default void onInfrastructureInitialization() {
        // empty by default
    }
}
