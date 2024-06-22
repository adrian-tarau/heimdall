package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class PingProvisioning implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingProvisioning.class);

    private final PingService pingService;
    private final InfrastructureService infrastructureService;
    private final PingRepository pingRepository;

    PingProvisioning(PingService pingService, InfrastructureService infrastructureService, PingRepository pingRepository) {
        requireNonNull(pingService);
        requireNonNull(infrastructureService);
        requireNonNull(pingRepository);
        this.pingService = pingService;
        this.infrastructureService = infrastructureService;
        this.pingRepository = pingRepository;
    }

    @Override
    public void run() {
        registerServers();
    }

    private void registerServers() {
        infrastructureService.getServers().forEach(this::registerServer);
    }

    private void registerServer(Server server) {
        if (!server.isIcmp()) return;
        Service service = Service.create(Service.Type.ICMP);
        try {
            if (!pingRepository.hasPing(server.getId(), service.getId())) {
                pingService.registerPing(service, server, service.getConnectionTimeout());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register ICMP ping for server '" + server.getName() + "'", e);
        }
    }
}
