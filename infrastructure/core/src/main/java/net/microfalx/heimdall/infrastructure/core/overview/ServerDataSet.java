package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.lang.annotation.Provider;

import java.util.stream.Collectors;

@Provider
public class ServerDataSet extends MemoryDataSet<Server, PojoField<Server>, String> {

    public ServerDataSet(DataSetFactory<Server, PojoField<Server>, String> factory, Metadata<Server, PojoField<Server>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Server> extractModels() {
        InfrastructureService infrastructureService = getService(InfrastructureService.class);
        MetadataService metadataService = getService(MetadataService.class);
        return infrastructureService.getServers().stream()
                .map(server -> from(infrastructureService, metadataService, server))
                .collect(Collectors.toList());
    }

    private Server from(InfrastructureService infrastructureService, MetadataService metadataService, net.microfalx.heimdall.infrastructure.api.Server server) {
        Server newCluster = new Server();
        metadataService.copy(server, newCluster);
        newCluster.setTimeZone(server.getZoneId().getId());
        newCluster.setServerCount(server.getServices().size());
        newCluster.setHealth(infrastructureService.getHealth(server));
        int degradedCount = 0;
        int unhealthyCount = 0;
        for (net.microfalx.heimdall.infrastructure.api.Service service : server.getServices()) {
            Health health = infrastructureService.getHealth(service);
            switch (health) {
                case DEGRADED -> degradedCount++;
                case HEALTHY -> unhealthyCount++;
            }
        }
        newCluster.setDegradedCount(degradedCount);
        newCluster.setUnhealthyCount(unhealthyCount);
        return newCluster;
    }
}
