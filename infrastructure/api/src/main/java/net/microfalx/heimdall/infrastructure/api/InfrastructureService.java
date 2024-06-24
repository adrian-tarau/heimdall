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
     * Returns the server with a given identifier.
     *
     * @param id the server identifier
     * @return the server
     * @throws InfrastructureNotFoundException if the server cannot be found
     */
    Server getServer(String id);

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
    Collection<Cluster> getClusters();

    /**
     * Returns the cluster with a given identifier.
     *
     * @param id the cluster identifier
     * @return the cluster
     * @throws InfrastructureNotFoundException if the server cannot be found
     */
    Cluster getCluster(String id);

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
     * Returns the service with a given identifier.
     *
     * @param id the service identifier
     * @return the service
     * @throws InfrastructureNotFoundException if the server cannot be found
     */
    Service getService(String id);

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
     * Returns the environment with a given identifier.
     *
     * @param id the environment identifier
     * @return the environment
     * @throws InfrastructureNotFoundException if the server cannot be found
     */
    Environment getEnvironment(String id);

    /**
     * Registers a new environment.
     *
     * @param environment the environment
     */
    void registerEnvironment(Environment environment);

    /**
     * Finds the environments which use a given server.
     *
     * @param server the server
     * @return a non-null instance
     */
    Collection<Environment> find(Server server);

    /**
     * Returns the status of a service running within a server.
     *
     * @param service the service
     * @param server  the server
     * @return a non-null instance
     */
    Status getStatus(Service service, Server server);

    /**
     * Returns the health of a service running within a server.
     *
     * @param environment the environment
     * @return a non-null instance
     */
    Health getHealth(Environment environment);

    /**
     * Returns the health of a service running within a server.
     *
     * @param service the service
     * @return a non-null instance
     */
    Health getHealth(Service service);

    /**
     * Returns the health of a service running within a server.
     *
     * @param server the server
     * @return a non-null instance
     */
    Health getHealth(Server server);

    /**
     * Returns the health of a service running within a server.
     *
     * @param service the service
     * @param server  the server
     * @return a non-null instance
     */
    Health getHealth(Service service, Server server);

    /**
     * Resolves the DNS for a given server.
     *
     * @param server the server
     * @return the DNS entry
     */
    Dns resolve(Server server);

    /**
     * Reloads the infrastructure.
     */
    void reload();
}
