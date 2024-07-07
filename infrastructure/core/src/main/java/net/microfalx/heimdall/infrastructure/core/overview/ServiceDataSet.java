package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.lang.annotation.Provider;

import java.util.Collection;
import java.util.stream.Collectors;

@Provider
public class ServiceDataSet extends MemoryDataSet<Service, PojoField<Service>, String> {

    public ServiceDataSet(DataSetFactory<Service, PojoField<Service>, String> factory, Metadata<Service, PojoField<Service>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Service> extractModels() {
        InfrastructureService infrastructureService = getService(InfrastructureService.class);
        MetadataService metadataService = getService(MetadataService.class);
        return infrastructureService.getServices().stream()
                .map(service -> from(infrastructureService, metadataService, service))
                .collect(Collectors.toList());
    }

    private Service from(InfrastructureService infrastructureService, MetadataService metadataService, net.microfalx.heimdall.infrastructure.api.Service service) {
        Service newService = new Service();
        metadataService.copy(service, newService);
        Collection<Server> servers = infrastructureService.getServers(service);
        newService.setServerCount(servers.size());
        newService.setHealth(infrastructureService.getHealth(service));
        newService.setActive(servers.size() > 0);
        int degradedCount = 0;
        int unhealthyCount = 0;
        for (net.microfalx.heimdall.infrastructure.api.Server server : servers) {
            Health health = infrastructureService.getHealth(service, server);
            switch (health) {
                case DEGRADED -> degradedCount++;
                case HEALTHY -> unhealthyCount++;
            }
        }
        newService.setDegradedCount(degradedCount);
        newService.setUnhealthyCount(unhealthyCount);
        return newService;
    }
}
