package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.heimdall.infrastructure.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class InfrastructureProvisioning {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureProvisioning.class);

    private final InfrastructurePersistence jpaManager;

    public InfrastructureProvisioning(InfrastructurePersistence jpaManager) {
        requireNonNull(jpaManager);
        this.jpaManager = jpaManager;
    }

    void execute() {
        try {
            provisionServices();
        } catch (Exception e) {
            LOGGER.error("Failed to provision default services", e);
        }
    }

    private void provisionServices() {
        jpaManager.execute(Service.create(Service.Type.HTTPS));
        jpaManager.execute(Service.create(Service.Type.SSH));
        jpaManager.execute(Service.create(Service.Type.ICMP));
    }

    private void provisionService() {

    }
}
