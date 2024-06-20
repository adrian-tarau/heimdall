package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.api.Status;
import net.microfalx.lang.TimeUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.TimeUtils.millisSince;

class InfrastructureHealth {

    private static final long STATUS_UPDATE = TimeUtils.TEN_SECONDS;

    private final Map<String, Status> statuses = new ConcurrentHashMap<>();
    private final Map<String, Long> statusLastUpdates = new ConcurrentHashMap<>();
    private final Map<String, Health> healths = new ConcurrentHashMap<>();
    private final Map<String, Long> healthLastUpdates = new ConcurrentHashMap<>();

    Status getStatus(Service service, Server server) {
        return statuses.getOrDefault(getKey(service, server), Status.NA);
    }

    void updateStatus(Service service, Server server, Status status) {
        String key = getKey(service, server);
        statusLastUpdates.put(key, currentTimeMillis());
        statuses.put(key, status);
    }

    boolean shouldUpdateStatus(Service service, Server server) {
        Long lastUpdated = statusLastUpdates.getOrDefault(getKey(service, server), TimeUtils.oneHourAgo());
        return millisSince(lastUpdated) > STATUS_UPDATE;
    }

    Health getHealth(Service service, Server server) {
        return healths.getOrDefault(getKey(service, server), Health.NA);
    }

    void updateHealth(Service service, Server server, Health health) {
        String key = getKey(service, server);
        healthLastUpdates.put(key, currentTimeMillis());
        healths.put(key, health);
    }

    boolean shouldUpdateHealth(Service service, Server server) {
        Long lastUpdated = healthLastUpdates.getOrDefault(getKey(service, server), TimeUtils.oneHourAgo());
        return millisSince(lastUpdated) > STATUS_UPDATE;
    }

    private String getKey(Service service, Server server) {
        return service.getId() + "_" + service.getId();
    }
}
