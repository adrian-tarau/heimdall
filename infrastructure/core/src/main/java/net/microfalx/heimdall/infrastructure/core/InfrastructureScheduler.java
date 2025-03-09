package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.heimdall.infrastructure.api.Cluster;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.core.util.HealthSummary;
import net.microfalx.threadpool.AbstractRunnable;

import static net.microfalx.lang.StringUtils.joinNames;

class InfrastructureScheduler extends AbstractRunnable {

    private final InfrastructureServiceImpl infrastructureService;
    private final InfrastructureHealth infrastructureHealth;

    InfrastructureScheduler(InfrastructureServiceImpl infrastructureService, InfrastructureHealth infrastructureHealth) {
        this.infrastructureService = infrastructureService;
        this.infrastructureHealth = infrastructureHealth;
        setName(joinNames("Infrastructure", "Scheduler"));
    }

    @Override
    public void run() {
        InfrastructureUtils.METRICS.time("Extract Health", (t) -> {
            updateEnvironments();
            updateClusters();
            updateServers();
            updateServices();
        });
    }

    private void updateEnvironments() {
        for (Environment environment : infrastructureService.getEnvironments()) {
            HealthSummary<?> healthSummary = infrastructureService.getHealthSummary(environment);
            infrastructureHealth.updateHealth(environment, healthSummary);
        }
    }

    private void updateClusters() {
        for (Cluster cluster : infrastructureService.getClusters()) {
            HealthSummary<?> healthSummary = infrastructureService.getHealthSummary(cluster);
            infrastructureHealth.updateHealth(cluster, healthSummary);
        }
    }

    private void updateServers() {
        for (Server server : infrastructureService.getServers()) {
            HealthSummary<?> healthSummary = infrastructureService.getHealthSummary(server);
            infrastructureHealth.updateHealth(server, healthSummary);
        }
    }

    private void updateServices() {
        for (Service service : infrastructureService.getServices()) {
            HealthSummary<?> healthSummary = infrastructureService.getHealthSummary(service);
            infrastructureHealth.updateHealth(service, healthSummary);
        }
    }
}
