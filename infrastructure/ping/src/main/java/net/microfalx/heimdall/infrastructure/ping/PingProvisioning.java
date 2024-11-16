package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Ping;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static java.time.Duration.ofSeconds;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class PingProvisioning implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingProvisioning.class);

    private final PingService pingService;
    private final InfrastructureService infrastructureService;

    private int count;

    PingProvisioning(PingService pingService, InfrastructureService infrastructureService) {
        requireNonNull(pingService);
        requireNonNull(infrastructureService);
        this.pingService = pingService;
        this.infrastructureService = infrastructureService;
    }

    @Override
    public void run() {
        registerServers();
        if (count > 0) pingService.reload();
    }

    private void registerServers() {
        infrastructureService.getServers().forEach(server -> {
            registerServer(server);
            pingService.getTaskExecutor().submit(() -> registerServices(server));
        });
    }

    private void registerServer(Server server) {
        if (!server.isIcmp()) return;
        Service service = Service.create(Service.Type.ICMP);
        try {
            if (pingService.registerPing(server.getName(), "Automatic registration",
                    service, server, ofSeconds(30))) {
                count++;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register ICMP ping for server '" + server.getName() + "'", e);
        }
    }

    private void registerServices(Server server) {
        Collection<Service> services = infrastructureService.getServices();
        for (Service service : services) {
            if (!service.isDiscoverable() || service.getType() == Service.Type.ICMP) continue;
            Ping ping = pingService.ping(service, server);
            if (!ping.getStatus().isFailure()) {
                try {
                    if (pingService.registerPing(service.getName() + " on " + server.getName(), "Automatic registration",
                            service, server, ofSeconds(30))) {
                        count++;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to register service '" + service.getName() + "' for server '" + server.getName() + "'", e);
                }
            } else {
                LOGGER.debug("Service '" + service.getName() + "' is not available on '" + server.getName() + "', status: " + ping.getStatus()
                        + ", error code: " + ping.getErrorCode() + ", error message: " + ping.getErrorMessage());
            }
        }
    }
}
