package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.ping.system.Ping;
import net.microfalx.heimdall.infrastructure.ping.system.PingRepository;
import net.microfalx.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableList;

/**
 * Loads ping related data from the database.
 */
@Component
class PingCache {

    @Autowired
    private PingRepository repository;

    private volatile List<Ping> pings = Collections.emptyList();
    private final Map<String, Ping> cachePingsByServiceAndServer = new ConcurrentHashMap<>();

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
        String cacheKey = PingUtils.getId(service, server);
        Ping jpaPing = cachePingsByServiceAndServer.get(cacheKey);
        if (jpaPing != null) return jpaPing;
        for (Ping ping : pings) {
            if (ObjectUtils.equals(ping.getServer().getNaturalId(), server.getId()) &&
                    ObjectUtils.equals(ping.getService().getNaturalId(), service.getId())) {
                jpaPing = ping;
                break;
            }
        }
        if (jpaPing != null) cachePingsByServiceAndServer.put(cacheKey, jpaPing);
        return jpaPing;
    }

    /**
     * Invoked when there is a need to reload the cache from database.
     */
    void reload() {
        pings = repository.findByActive(true);
        cachePingsByServiceAndServer.clear();
    }
}
