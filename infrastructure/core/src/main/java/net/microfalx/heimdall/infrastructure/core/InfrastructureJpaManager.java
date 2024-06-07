package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.jdbc.jpa.NaturalIdEntityUpdater;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.lang.ExceptionUtils;

class InfrastructureJpaManager extends ApplicationContextSupport {

    void execute(net.microfalx.heimdall.infrastructure.api.Environment environment) {
        NaturalIdEntityUpdater<Environment, Integer> updater = getUpdater(EnvironmentRepository.class);
        Environment jpaEnvironment = new Environment();
        jpaEnvironment.setNaturalId(environment.getId());
        jpaEnvironment.setName(environment.getName());
        jpaEnvironment.setDescription(environment.getDescription());
        jpaEnvironment.setAttributes(ExceptionUtils.doAndRethrow(() -> environment.getAttributes().toJson().loadAsString()));
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
        return updater.findByNaturalIdAndUpdate(jpaCluster);
    }

    void execute(net.microfalx.heimdall.infrastructure.api.Server server, net.microfalx.heimdall.infrastructure.api.Cluster cluster) {
        NaturalIdEntityUpdater<Server, Integer> updater = getUpdater(ServerRepository.class);
        Server jpaServer = new Server();
        jpaServer.setNaturalId(server.getId());
        jpaServer.setName(server.getName());
        jpaServer.setDescription(server.getDescription());
        jpaServer.setHostname(server.getHostname());
        jpaServer.setIcmp(server.isIcmp());
        if (cluster != null) {
            Cluster jpaCluster = execute(cluster);
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
        jpaService.setPath(service.getPath());
        jpaService.setAuthType(service.getAuthType());
        jpaService.setUsername(service.getUserName());
        jpaService.setPassword(service.getPassword());
        jpaService.setToken(service.getToken());
        updater.findByNaturalIdAndUpdate(jpaService);
    }

    private <M, ID> NaturalIdEntityUpdater<M, ID> getUpdater(Class<? extends NaturalJpaRepository<M, ID>> repositoryClass) {
        NaturalJpaRepository<M, ID> repository = getBean(repositoryClass);
        NaturalIdEntityUpdater<M, ID> updater = new NaturalIdEntityUpdater<>(getBean(MetadataService.class), repository);
        updater.setApplicationContext(getApplicationContext());
        return updater;
    }
}
