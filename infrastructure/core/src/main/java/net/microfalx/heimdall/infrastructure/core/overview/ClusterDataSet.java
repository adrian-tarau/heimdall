package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.core.util.HealthSummary;
import net.microfalx.lang.annotation.Provider;

import java.util.stream.Collectors;

@Provider
public class ClusterDataSet extends MemoryDataSet<Cluster, PojoField<Cluster>, String> {

    public ClusterDataSet(DataSetFactory<Cluster, PojoField<Cluster>, String> factory, Metadata<Cluster, PojoField<Cluster>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<Cluster> extractModels() {
        InfrastructureService infrastructureService = getService(InfrastructureService.class);
        MetadataService metadataService = getService(MetadataService.class);
        return infrastructureService.getClusters().stream()
                .map(cluster -> from(infrastructureService, metadataService, cluster))
                .collect(Collectors.toList());
    }

    private Cluster from(InfrastructureService infrastructureService, MetadataService metadataService, net.microfalx.heimdall.infrastructure.api.Cluster cluster) {
        Cluster newCluster = new Cluster();
        metadataService.copy(cluster, newCluster);
        newCluster.setReference(cluster);
        newCluster.setTimeZone(cluster.getZoneId().getId());
        newCluster.setHealth(infrastructureService.getHealth(cluster));

        HealthSummary<net.microfalx.heimdall.infrastructure.api.Server> healthSummary = new HealthSummary<>(infrastructureService::getHealth);
        healthSummary.inspect(cluster.getServers());
        newCluster.setTotalCount(healthSummary.getTotalCount());
        newCluster.setUnavailableCount(healthSummary.getUnavailableCount());
        newCluster.setDegradedCount(healthSummary.getDegradedCount());
        newCluster.setUnhealthyCount(healthSummary.getUnhealthyCount());
        return newCluster;
    }
}
