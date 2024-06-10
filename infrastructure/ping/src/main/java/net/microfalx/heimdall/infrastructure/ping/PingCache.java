package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Loads ping related data from the database.
 */
@Component
class PingCache implements InfrastructureListener {

    @Autowired
    private PingRepository repository;

    private volatile List<Ping> pings = Collections.emptyList();

    /**
     * Returns the active pings.
     *
     * @return a non-null instance
     */
    List<Ping> getPings() {
        return unmodifiableList(pings);
    }

    /**
     * Returns the ping JPA based on a service and a server.
     *
     * @param service the service
     * @param server  the server
     * @return the ping
     */
    Ping find(Service service, Server server) {
        Ping jpaPing=null;
        for (Ping ping:pings){
            if (ping.getServer().getNaturalId().equals(server.getId()) &&
                    ping.getService().getNaturalId().equals(service.getId())){
                jpaPing=ping;
                break;
            }
        }
        return jpaPing;
    }

    /**
     * Invoked when there is a need to reload the cache from database.
     */
    void reload() {
        pings = repository.findAll();
    }

    @Override
    public void onEnvironmentChanged(Environment environment) {
        reload();
    }

    @Override
    public void onClusterChanged(Cluster cluster) {
        reload();
    }

    @Override
    public void onServiceChanged(Service service) {
        reload();
    }
}
