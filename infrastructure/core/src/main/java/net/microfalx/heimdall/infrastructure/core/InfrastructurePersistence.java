package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.heimdall.infrastructure.core.system.*;
import net.microfalx.lang.ExceptionUtils;

import static net.microfalx.lang.CollectionUtils.setToString;
import static net.microfalx.lang.NamedAndTaggedIdentifyAware.AUTO_TAG;

class InfrastructurePersistence extends ApplicationContextSupport {

    void execute(net.microfalx.heimdall.infrastructure.api.Environment environment) {
        NaturalIdEntityUpdater<Environment, Integer> updater = getUpdater(EnvironmentRepository.class);
        Environment jpaEnvironment = new Environment();
        jpaEnvironment.setNaturalId(environment.getId());
        jpaEnvironment.setName(environment.getName());
        jpaEnvironment.setBaseUri(environment.getBaseUri().toASCIIString());
        jpaEnvironment.setAppPath(environment.getAppPath());
        jpaEnvironment.setApiPath(environment.getApiPath());
        jpaEnvironment.setTags(setToString(environment.getTags()));
        jpaEnvironment.setDescription(environment.getDescription());
        jpaEnvironment.setAttributes(ExceptionUtils.doAndRethrow(() -> environment.getAttributes(false).toProperties().loadAsString()));
        updater.findByNaturalIdAndUpdate(jpaEnvironment);
    }

    Cluster execute(net.microfalx.heimdall.infrastructure.api.Cluster cluster) {
        NaturalIdEntityUpdater<Cluster, Integer> updater = getUpdater(ClusterRepository.class);
        Cluster jpaCluster = new Cluster();
        jpaCluster.setNaturalId(cluster.getId());
        jpaCluster.setName(cluster.getName());
        jpaCluster.setTimeZone(cluster.getZoneId().getId());
        jpaCluster.setType(cluster.getType());
        jpaCluster.setDescription(cluster.getDescription());
        jpaCluster.setTags(setToString(cluster.getTags()));
        jpaCluster = updater.findByNaturalIdAndUpdate(jpaCluster);
        return jpaCluster;
    }

    void execute(net.microfalx.heimdall.infrastructure.api.Server server, net.microfalx.heimdall.infrastructure.api.Cluster cluster, Cluster jpaCluster) {
        NaturalIdEntityUpdater<Server, Integer> updater = getUpdater(ServerRepository.class);
        Server jpaServer = new Server();
        jpaServer.setNaturalId(server.getId());
        jpaServer.setName(server.getName());
        jpaServer.setHostname(server.getHostname());
        jpaServer.setTags(setToString(server.getTags()));
        jpaServer.setTimeZone(server.getZoneId().getId());
        jpaServer.setIcmp(server.isIcmp());
        jpaServer.setAttributes(ExceptionUtils.doAndRethrow(() -> server.getAttributes().toProperties().loadAsString()));
        jpaServer.setDescription(server.getDescription());
        if (jpaCluster != null) {
            jpaServer.setCluster(jpaCluster);
        } else if (cluster != null) {
            jpaCluster = execute(cluster);
            jpaServer.setCluster(jpaCluster);
        }
        updater.findByNaturalIdAndUpdate(jpaServer);
    }

    void execute(net.microfalx.heimdall.infrastructure.api.Service service) {
        NaturalIdEntityUpdater<Service, Integer> updater = getUpdater(ServiceRepository.class);
        Service jpaService = new Service();
        jpaService.setNaturalId(service.getId());
        jpaService.setName(service.getName());
        jpaService.setDescription(service.getDescription());
        jpaService.setType(service.getType());
        jpaService.setPort(service.getPort());
        jpaService.setPath(service.getBasePath());
        jpaService.setLivenessPath(service.getLivenessPath());
        jpaService.setReadinessPath(service.getReadinessPath());
        jpaService.setMetricsPath(service.getMetricsPath());
        jpaService.setAuthType(service.getAuthType());
        jpaService.setUsername(service.getUserName());
        jpaService.setPassword(service.getPassword());
        jpaService.setToken(service.getToken());
        jpaService.setDiscoverable(service.isDiscoverable());
        jpaService.setTls(service.isTls());
        jpaService.setTags(setToString(service.getTags()));
        jpaService.setConnectionTimeOut((int) service.getConnectionTimeout().toMillis());
        jpaService.setReadTimeOut((int) service.getReadTimeout().toMillis());
        jpaService.setWriteTimeOut((int) service.getWriteTimeout().toMillis());
        updater.findByNaturalIdAndUpdate(jpaService);
        if (!service.getTags().contains(AUTO_TAG)) {
            updater.findByNaturalIdAndUpdate(jpaService);
        } else {
            updater.findByNaturalIdOrCreate(jpaService);
        }
    }

    private <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(Class<? extends NaturalJpaRepository<M, ID>> repositoryClass) {
        NaturalJpaRepository<M, ID> repository = getBean(repositoryClass);
        NaturalIdEntityUpdater<M, ID> updater = new NaturalIdEntityUpdater<>(getBean(MetadataService.class), repository);
        updater.setApplicationContext(getApplicationContext());
        return updater;
    }
}
