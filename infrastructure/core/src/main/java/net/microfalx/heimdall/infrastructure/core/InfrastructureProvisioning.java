package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class InfrastructureProvisioning implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureProvisioning.class);

    private final InfrastructureService infrastructureService;

    public InfrastructureProvisioning(InfrastructureService infrastructureService) {
        requireNonNull(infrastructureService);
        this.infrastructureService = infrastructureService;
    }

    @Override
    public void run() {
        try {
            provisionServices();
        } catch (Exception e) {
            LOGGER.error("Failed to provision default services", e);
        }
    }

    private void provisionServices() {
        infrastructureService.registerService(Service.create(Service.Type.HTTPS));
        infrastructureService.registerService(Service.create(Service.Type.SSH));
        infrastructureService.registerService(Service.create(Service.Type.ICMP));
    }

    private void provisionService() {

    }
}
