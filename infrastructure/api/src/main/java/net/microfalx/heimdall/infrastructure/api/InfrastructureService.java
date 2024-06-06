package net.microfalx.heimdall.infrastructure.api;

import java.util.Collection;

/**
 * A service which manages the infrastructure.
 */
public interface InfrastructureService {

    /**
     * Returns registered servers.
     *
     * @return a non-null instance
     */
    Collection<Server> getServers();

    /**
     * Registers a new server.
     *
     * @param server the server
     */
    void registerServer(Server server);

    /**
     * Returns registered servers.
     *
     * @return a non-null instance
     */
    Collection<Server> getClusters();

    /**
     * Registers a new cluster.
     *
     * @param cluster the cluster
     */
    void registerCluster(Cluster cluster);

    /**
     * Returns registered servers.
     *
     * @return a non-null instance
     */
    Collection<Service> getServices();

    /**
     * Registers a new cluster.
     *
     * @param service the service
     */
    void registerService(Service service);

    /**
     * Returns registered services.
     *
     * @return a non-null instance
     */
    Collection<Environment> getEnvironments();

    /**
     * Registers a new environment.
     *
     * @param environment the environment
     */
    void registerEnvironment(Environment environment);

    /**
     * Reloads the infrastructure.
     */
    void reload();
}
