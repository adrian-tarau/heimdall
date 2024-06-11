package net.microfalx.heimdall.infrastructure.core;

/**
 * A class responsible for loading the infrastructure from the database.
 */
class InfrastructureLoader {

    InfrastructureCache load() {
        InfrastructureCache cache = new InfrastructureCache();
        return cache;
    }

}
