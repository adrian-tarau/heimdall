package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.core.util.HealthSummary;
import net.microfalx.lang.annotation.Provider;

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
        newService.setHealth(infrastructureService.getHealth(service));
        HealthSummary<Server> healthSummary = new HealthSummary<>(infrastructureService::getHealth);
        healthSummary.inspect(infrastructureService.getServers(service));
        newService.setServerCount(healthSummary.getTotalCount());
        newService.setUnavailableCount(healthSummary.getUnavailableCount());
        newService.setDegradedCount(healthSummary.getDegradedCount());
        newService.setUnhealthyCount(healthSummary.getUnhealthyCount());
        return newService;
    }
}
