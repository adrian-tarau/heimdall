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

    /**
     * Queries for the status of a service.
     *
     * @param service the service
     * @param server  the server where the server runs
     * @return the status, null if the listener cannot provide a status
     */
    default Status getStatus(Service service, Server server) {
        return Status.NA;
    }

    /**
     * Queries for the health of a service.
     *
     * @param service the service
     * @param server  the server where the server runs
     * @return the health, null if the listener cannot provide a health
     */
    default Health getHealth(Service service, Server server) {
        return Health.NA;
    }
}
