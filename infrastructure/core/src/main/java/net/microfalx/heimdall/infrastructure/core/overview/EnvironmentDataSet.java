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
public class EnvironmentDataSet extends MemoryDataSet<Environment, PojoField<Environment>, String> {

    public EnvironmentDataSet(DataSetFactory<Environment, PojoField<Environment>, String> factory, Metadata<Environment, PojoField<Environment>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Environment> extractModels() {
        InfrastructureService infrastructureService = getService(InfrastructureService.class);
        MetadataService metadataService = getService(MetadataService.class);
        return infrastructureService.getEnvironments().stream()
                .map(environment -> from(infrastructureService, metadataService, environment))
                .collect(Collectors.toList());
    }

    private Environment from(InfrastructureService infrastructureService, MetadataService metadataService, net.microfalx.heimdall.infrastructure.api.Environment environment) {
        Environment newEnvironment = new Environment();
        metadataService.copy(environment, newEnvironment);
        newEnvironment.setReference(environment);
        newEnvironment.setHealth(infrastructureService.getHealth(environment));

        HealthSummary<Server> healthSummary = new HealthSummary<>(infrastructureService::getHealth);
        healthSummary.inspect(environment.getServers());
        newEnvironment.setTotalCount(healthSummary.getTotalCount());
        newEnvironment.setUnavailableCount(healthSummary.getUnavailableCount());
        newEnvironment.setDegradedCount(healthSummary.getDegradedCount());
        newEnvironment.setUnhealthyCount(healthSummary.getUnhealthyCount());
        return newEnvironment;
    }
}
