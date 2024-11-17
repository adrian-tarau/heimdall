package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.model.AttributeUtils;
import net.microfalx.heimdall.infrastructure.api.*;
import net.microfalx.heimdall.infrastructure.core.system.ClusterRepository;
import net.microfalx.heimdall.infrastructure.core.system.EnvironmentRepository;
import net.microfalx.heimdall.infrastructure.core.system.ServerRepository;
import net.microfalx.heimdall.infrastructure.core.system.ServiceRepository;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.MemoryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.*;

import static java.time.Duration.ofMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.CollectionUtils.setFromString;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

class InfrastructureCache extends ApplicationContextSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureCache.class);

    private final InfrastructureServiceImpl infrastructureService;

    private final Map<String, Server> servers = new HashMap<>();
    private final Map<String, Cluster> clusters = new HashMap<>();
    private final Map<String, net.microfalx.heimdall.infrastructure.api.Service> services = new HashMap<>();
    private final Map<String, Environment> environments = new HashMap<>();

    InfrastructureCache(InfrastructureServiceImpl infrastructureService) {
        this.infrastructureService = infrastructureService;
        registerServer(Server.LOCAL);
    }

    Map<String, Server> getServers() {
        return servers;
    }

    void registerServer(Server server) {
        servers.put(toIdentifier(server.getId()), server);
    }

    Server getServer(String id) {
        requireNonNull(id);
        Server server = servers.get(toIdentifier(id));
        if (server == null) {
            throw new InfrastructureNotFoundException("A server with identifier '" + id + "' is not registered");
        }
        return server;
    }

    void registerCluster(Cluster cluster) {
        clusters.put(toIdentifier(cluster.getId()), cluster);
        for (Server server : cluster.getServers()) {
            registerServer(server);
        }
    }

    Map<String, Cluster> getClusters() {
        return clusters;
    }

    Cluster findByName(String name) {
        for (Cluster cluster : clusters.values()) {
            if (cluster.getName().equalsIgnoreCase(name)) return cluster;
        }
        return null;
    }

    Cluster getCluster(String id) {
        requireNonNull(id);
        Cluster cluster = clusters.get(toIdentifier(id));
        if (cluster == null) {
            throw new InfrastructureNotFoundException("A cluster with identifier '" + id + "' is not registered");
        }
        return cluster;
    }

    void registerService(Service service) {
        services.put(toIdentifier(service.getId()), service);
    }

    Map<String, Service> getServices() {
        return services;
    }

    net.microfalx.heimdall.infrastructure.api.Service getService(String id) {
        requireNonNull(id);
        net.microfalx.heimdall.infrastructure.api.Service service = services.get(toIdentifier(id));
        if (service == null) {
            throw new InfrastructureNotFoundException("A service with identifier '" + id + "' is not registered");
        }
        return service;
    }

    void registerEnvironment(Environment environment) {
        environments.put(toIdentifier(environment.getId()), environment);
    }

    Map<String, Environment> getEnvironments() {
        return environments;
    }

    Environment getEnvironment(String id) {
        requireNonNull(id);
        Environment environment = environments.get(toIdentifier(id));
        if (environment == null) {
            throw new InfrastructureNotFoundException("A environment with identifier '" + id + "' is not registered");
        }
        return environment;
    }

    Collection<Environment> find(Server server) {
        requireNonNull(server);
        Collection<Environment> foundEnvironments = new ArrayList<>();
        for (Environment environment : environments.values()) {
            if (environment.hasServer(server)) foundEnvironments.add(environment);
        }
        return foundEnvironments;
    }

    Collection<Server> getServers(Service service) {
        Set<Server> serversWithService = new HashSet<>();
        for (Server server : getServers().values()) {
            for (Service runningService : server.getServices()) {
                if (runningService.equals(service)) {
                    serversWithService.add(server);
                }
            }
        }
        return serversWithService;
    }

    void load() {
        LOGGER.info("Load infrastructure");
        try {
            loadServices();
        } catch (Exception e) {
            LOGGER.error("Failed to load services", e);
        }
        try {
            Map<Integer, Set<Server>> serversByCluster = loadServers();
            loadClusters(serversByCluster);
        } catch (Exception e) {
            LOGGER.error("Failed to load servers & clusters", e);
        }
        try {
            loadEnvironment();
        } catch (Exception e) {
            LOGGER.error("Failed to load environments", e);
        }
        LOGGER.info("Infrastructure loaded, environment: {}, clusters: {}, servers: {}, services: {}",
                environments.size(), clusters.size(), servers.size(), services.size());
    }

    private void loadServices() {
        List<net.microfalx.heimdall.infrastructure.core.system.Service> serviceJpas = getBean(ServiceRepository.class).findAll();
        for (net.microfalx.heimdall.infrastructure.core.system.Service serviceJpa : serviceJpas) {
            Service.Builder builder = new Service.Builder(serviceJpa.getNaturalId())
                    .path(serviceJpa.getPath()).port(serviceJpa.getPort()).type(serviceJpa.getType());
            if (isNotEmpty(serviceJpa.getUsername())) builder.user(serviceJpa.getUsername(), serviceJpa.getPassword());
            if (isNotEmpty(serviceJpa.getToken())) builder.token(serviceJpa.getToken());
            builder.discoverable(serviceJpa.isDiscoverable()).tls(serviceJpa.isDiscoverable())
                    .authType(serviceJpa.getAuthType())
                    .connectionTimeout(ofMillis(serviceJpa.getConnectionTimeOut()))
                    .readTimeout(ofMillis(serviceJpa.getReadTimeOut())).writeTimeout(ofMillis(serviceJpa.getWriteTimeOut()));
            builder.livenessPath(serviceJpa.getLivenessPath()).readinessPath(serviceJpa.getReadinessPath())
                    .metricsPath(serviceJpa.getMetricsPath());
            builder.tags(setFromString(serviceJpa.getTags())).name(serviceJpa.getName()).description(serviceJpa.getDescription());
            registerService(builder.build());
        }
    }

    private Map<Integer, Set<Server>> loadServers() {
        Map<Integer, Set<Server>> serversByCluster = new HashMap<>();
        List<net.microfalx.heimdall.infrastructure.core.system.Server> serversJpas = getBean(ServerRepository.class).findAll();
        for (net.microfalx.heimdall.infrastructure.core.system.Server serversJpa : serversJpas) {
            net.microfalx.heimdall.infrastructure.core.system.Cluster clusterJpa = serversJpa.getCluster();
            Server.Builder builder = new Server.Builder(serversJpa.getNaturalId()).type(serversJpa.getType())
                    .icmp(serversJpa.isIcmp()).hostname(serversJpa.getHostname());
            if (clusterJpa != null) builder.zoneId(ZoneId.of(clusterJpa.getTimeZone()));
            builder.attributes(ExceptionUtils.doAndRethrow(() -> AttributeUtils.decodeProperties(MemoryResource.create(serversJpa.getAttributes()))));
            builder.zoneId(ZoneId.of(serversJpa.getTimeZone())).tags(setFromString(serversJpa.getTags()))
                    .name(serversJpa.getName()).description(serversJpa.getDescription());
            Server server = builder.build();
            registerServer(server);
            if (clusterJpa != null) {
                serversByCluster.computeIfAbsent(clusterJpa.getId(), integer -> new HashSet<>()).add(server);
            }
        }
        return serversByCluster;
    }

    private void loadClusters(Map<Integer, Set<Server>> serversByCluster) {
        List<net.microfalx.heimdall.infrastructure.core.system.Cluster> clusterJpas = getBean(ClusterRepository.class).findAll();
        for (net.microfalx.heimdall.infrastructure.core.system.Cluster clusterJpa : clusterJpas) {
            Cluster.Builder builder = new Cluster.Builder(clusterJpa.getNaturalId()).zoneId(ZoneId.of(clusterJpa.getTimeZone()));
            builder.tags(setFromString(clusterJpa.getTags())).name(clusterJpa.getName()).description(clusterJpa.getDescription());
            Set<Server> servers = serversByCluster.getOrDefault(clusterJpa.getId(), Collections.emptySet());
            builder.servers(servers);
            registerCluster(builder.build());
        }
    }

    private void loadEnvironment() {
        List<net.microfalx.heimdall.infrastructure.core.system.Environment> environmentsJpas
                = getBean(EnvironmentRepository.class).findAll();
        environmentsJpas.forEach(e -> {
            Environment.Builder builder = new Environment.Builder(e.getNaturalId());
            builder.tags(setFromString(e.getTags())).name(e.getName()).description(e.getDescription());
            builder.baseUri(e.getBaseUri()).apiPath(e.getApiPath()).appPath(e.getAppPath());
            registerEnvironment(builder.build());
        });
    }
}
