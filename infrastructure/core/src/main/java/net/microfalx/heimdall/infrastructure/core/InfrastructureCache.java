package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.heimdall.infrastructure.api.Cluster;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.api.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

class InfrastructureCache {

    private final Map<String, Server> servers = new HashMap<>();
    private final Map<String, Cluster> clusters = new HashMap<>();
    private final Map<String, net.microfalx.heimdall.infrastructure.api.Service> services = new HashMap<>();
    private final Map<String, Environment> environments = new HashMap<>();

    Map<String, Server> getServers() {
        return servers;
    }

    Server getServer(String id) {
        requireNonNull(id);
        Server server = servers.get(toIdentifier(id));
        if (server == null) {
            throw new InfrastructureNotFoundException("A server with identifier '" + id + "' is not registered");
        }
        return server;
    }

    Map<String, Cluster> getClusters() {
        return clusters;
    }

    Cluster getCluster(String id) {
        requireNonNull(id);
        Cluster cluster = clusters.get(toIdentifier(id));
        if (cluster == null) {
            throw new InfrastructureNotFoundException("A cluster with identifier '" + id + "' is not registered");
        }
        return cluster;
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
}
