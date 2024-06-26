package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.Ping;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableMap;

/**
 * A class which monitors the outcome of pings and calculates the health of services.
 */
@Component
public class PingHealth {

    private static final Deque<Ping> EMPTY_PINGS = new LinkedList<>();

    @Autowired
    private PingProperties properties;

    private final Map<String, Queue<Ping>> pings = new ConcurrentHashMap<>();
    private final Map<String, Ping> lastPings = new ConcurrentHashMap<>();

    /**
     * Returns the tracked pings.
     *
     * @return a non-null instance
     */
    Map<String, Queue<Ping>> getPings() {
        return unmodifiableMap(pings);
    }

    /**
     * Registers the outcome of a ping.
     *
     * @param ping the ping
     */
    void registerPing(Ping ping) {
        Queue<Ping> queue = pings.computeIfAbsent(ping.getId(), pings -> new ArrayBlockingQueue<>(properties.getWindowSize()));
        if (!queue.offer(ping)) {
            queue.remove();
            queue.offer(ping);
        }
        lastPings.put(ping.getId(), ping);
    }

    /**
     * Retries the status of the last ping.
     *
     * @param service the service
     * @param server  the server
     * @return a non-null status
     */
    Status getStatus(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        Ping ping = lastPings.get(PingUtils.getId(service, server));
        return ping == null ? Status.NA : ping.getStatus();
    }

    Health getHealth(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        Queue<Ping> queue = getPingQueue(service, server);
        if (queue.isEmpty()) return Health.NA;
        int numberOfFailPings = 0;
        for (Ping ping : queue) {
            if (ping.getStatus().isFailure()) numberOfFailPings++;
        }
        if (queue.size() != properties.getWindowSize()) {
            if (numberOfFailPings == 0) {
                return Health.HEALTHY;
            } else {
                return Health.NA;
            }
        } else {
            float percentageOfFailPings = ((float) numberOfFailPings / properties.getWindowSize()) * 100;
            if (percentageOfFailPings >= properties.getUnhealthyThreshold()) {
                return Health.UNHEALTHY;
            } else if (percentageOfFailPings >= properties.getDegradedThreshold()) {
                return Health.DEGRADED;
            } else {
                return Health.HEALTHY;
            }
        }
    }

    private Queue<Ping> getPingQueue(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        return pings.getOrDefault(PingUtils.getId(service, server), EMPTY_PINGS);
    }


}
