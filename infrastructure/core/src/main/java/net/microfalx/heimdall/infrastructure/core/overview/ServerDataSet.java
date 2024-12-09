package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.core.util.HealthSummary;
import net.microfalx.lang.annotation.Provider;

import java.util.stream.Collectors;

@Provider
public class ServerDataSet extends MemoryDataSet<Server, PojoField<Server>, String> {

    public ServerDataSet(DataSetFactory<Server, PojoField<Server>, String> factory, Metadata<Server, PojoField<Server>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Server> extractModels(Filter filterable) {
        InfrastructureService infrastructureService = getService(InfrastructureService.class);
        MetadataService metadataService = getService(MetadataService.class);
        return infrastructureService.getServers().stream()
                .map(server -> from(infrastructureService, metadataService, server))
                .collect(Collectors.toList());
    }

    private Server from(InfrastructureService infrastructureService, MetadataService metadataService, net.microfalx.heimdall.infrastructure.api.Server server) {
        Server newCluster = new Server();
        metadataService.copy(server, newCluster);
        newCluster.setReference(server);
        newCluster.setTimeZone(server.getZoneId().getId());
        newCluster.setHealth(infrastructureService.getHealth(server));

        HealthSummary<Service> healthSummary = new HealthSummary<>(infrastructureService::getHealth);
        healthSummary.inspect(server.getServices());
        newCluster.setTotalCount(healthSummary.getTotalCount());
        newCluster.setUnavailableCount(healthSummary.getUnavailableCount());
        newCluster.setDegradedCount(healthSummary.getDegradedCount());
        newCluster.setUnhealthyCount(healthSummary.getUnhealthyCount());
        return newCluster;
    }
}
