package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.bootstrap.metrics.Series;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.infrastructure.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Duration.ofMillis;
import static java.util.Collections.unmodifiableCollection;
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
    public Map<String, Queue<Ping>> getPings() {
        return unmodifiableMap(pings);
    }

    /**
     * Returns the last pings.
     *
     * @return a non-null
     */
    public Collection<Ping> getLastPings() {
        return unmodifiableCollection(lastPings.values());
    }

    /**
     * Return the last duration of the ping
     *
     * @param service the service
     * @param server  the server
     * @return the last duration of the ping
     */
    public Duration getLastDuration(Service service, Server server) {
        Queue<Ping> pingQueue = getPingQueue(service, server);
        Ping ping = (Ping) pingQueue.toArray()[pingQueue.toArray().length - 1];
        return ping.getDuration();
    }

    /**
     * Return the minimum duration of the ping
     *
     * @param service the service
     * @param server  the server t
     * @return the minimum duration of the ping
     */
    public Duration getMinDuration(Service service, Server server) {
        Queue<Ping> pingQueue = getPingQueue(service, server);
        double duration = pingQueue.stream().mapToLong(p -> p.getDuration().toMillis()).min().orElse(0);
        return ofMillis((long) duration);
    }

    /**
     * Return the maximum duration of the ping
     *
     * @param service the service
     * @param server  the server
     * @return the maximum duration of the ping
     */
    public Duration getMaxDuration(Service service, Server server) {
        Queue<Ping> pingQueue = getPingQueue(service, server);
        double duration = pingQueue.stream().mapToLong(p -> p.getDuration().toMillis()).max().orElse(0);
        return ofMillis((long) duration);
    }

    /**
     * Return the average duration of the ping
     *
     * @param service the service
     * @param server  the server
     * @return the average duration of the ping
     */
    public Duration getAverageDuration(Service service, Server server) {
        Queue<Ping> pingQueue = getPingQueue(service, server);
        double duration = pingQueue.stream().mapToLong(p -> p.getDuration().toMillis()).average().orElse(0);
        return ofMillis((long) duration);
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
     * Returns a (time) series with the ping results for a given service and server.
     *
     * @param service the service
     * @param server  the server
     * @return a non-null series
     */
    Series getSeries(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        Queue<Ping> pingQueue = getPingQueue(service, server);
        List<Value> values = pingQueue.stream().map(ping -> {
            if (ping.getStatus().isFailure()) {
                return Value.create(ping.getStartedAt().toLocalDateTime(), -1 * ping.getDuration().toMillis());
            }
            return Value.create(ping.getStartedAt().toLocalDateTime(), ping.getDuration().toMillis());
        }).toList();
        return Series.create(PingUtils.getName(service, server), values);
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
