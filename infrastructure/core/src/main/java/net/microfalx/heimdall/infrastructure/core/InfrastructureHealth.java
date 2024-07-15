package net.microfalx.heimdall.infrastructure.core;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.bootstrap.metrics.Series;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.heimdall.infrastructure.api.*;
import net.microfalx.heimdall.infrastructure.core.util.HealthSummary;
import net.microfalx.lang.TimeUtils;
import net.microfalx.lang.Timestampable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.lang.TimeUtils.millisSince;

@Component
class InfrastructureHealth {

    private static final long STATUS_UPDATE = TimeUtils.TEN_SECONDS;
    private static final Queue<Health> EMPTY_HEALTH = new ArrayDeque<>();
    private static final Queue<HealthCounts> EMPTY_HEALTH_COUNTS = new ArrayDeque<>();

    @Autowired
    private InfrastructureProperties properties;

    private final Map<String, Status> statuses = new ConcurrentHashMap<>();
    private final Map<String, Long> statusLastUpdates = new ConcurrentHashMap<>();
    private final Map<String, Health> healths = new ConcurrentHashMap<>();
    private final Map<String, Long> healthLastUpdates = new ConcurrentHashMap<>();

    private final Map<String, Queue<Health>> healthQueues = new ConcurrentHashMap<>();
    private final Map<String, Queue<HealthCounts>> healthCountQueues = new ConcurrentHashMap<>();

    Status getStatus(Service service, Server server) {
        return statuses.getOrDefault(getKey(service, server), Status.NA);
    }

    Collection<Health> getHealthTrend(InfrastructureElement element) {
        requireNonNull(element);
        return healthQueues.getOrDefault(getKey(element), EMPTY_HEALTH);
    }

    Series getHealthTrend(InfrastructureElement element, Health health) {
        requireNonNull(element);
        requireNonNull(health);
        Collection<HealthCounts> healthCounts = healthCountQueues.getOrDefault(getKey(element), EMPTY_HEALTH_COUNTS);
        return Series.create("Health Counts", healthCounts.stream()
                .map(hc -> Value.create(hc.timestamp, hc.getCount(health)))
                .collect(Collectors.toList()));
    }

    void updateStatus(Service service, Server server, Status status) {
        String key = getKey(service, server);
        statusLastUpdates.put(key, currentTimeMillis());
        statuses.put(key, status);
    }

    <T extends InfrastructureElement> void updateHealth(InfrastructureElement element, HealthSummary<T> healthSummary) {
        requireNonNull(element);
        String key = getKey(element);
        Queue<Health> healthQueue = healthQueues.computeIfAbsent(key, k -> new ArrayBlockingQueue<>(properties.getWindowSize()));
        queueElement(healthQueue, healthSummary.getHealth());
        Queue<HealthCounts> healthCountsQueue = healthCountQueues.computeIfAbsent(key, k -> createHealthCountsQueue());
        queueElement(healthCountsQueue, new HealthCounts(healthSummary, healthSummary.getCreatedAt()));
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

    private Queue<HealthCounts> createHealthCountsQueue() {
        Duration duration = Duration.ofSeconds(30);
        Queue<HealthCounts> queue = new ArrayBlockingQueue<>(properties.getWindowSize());
        LocalDateTime timestamp = LocalDateTime.now().minus(duration.multipliedBy(properties.getWindowSize()));
        for(int i = 0; i < properties.getWindowSize();i++) {
            queue.offer(new HealthCounts(TimeUtils.toMillis(timestamp), Health.NA));
            timestamp = timestamp.plus(duration);
        }
        return queue;
    }

    private String getKey(Service service, Server server) {
        return service.getId() + "_" + service.getId();
    }

    private String getKey(InfrastructureElement element) {
        return toIdentifier(element.getClass().getSimpleName()) + "_" + element.getId();
    }

    private <T> void queueElement(Queue<T> queue, T element) {
        if (!queue.offer(element)) {
            queue.remove();
            queue.offer(element);
        }
    }

    @ToString
    @Getter
    static class HealthCounts implements HealthAware, Timestampable<LocalDateTime> {

        private final long timestamp;
        private final Health health;
        private final int totalCount;
        private final int unavailableCount;
        private final int unhealthyCount;
        private final int degradedCount;

        public HealthCounts(long timestamp, Health health) {
            this.timestamp = timestamp;
            this.health = health;
            totalCount = 0;
            unavailableCount = 0;
            unhealthyCount = 0;
            degradedCount = 0;
        }

        HealthCounts(HealthAware healthAware, LocalDateTime timestamp) {
            this.timestamp = TimeUtils.toMillis(timestamp);
            health = healthAware.getHealth();
            totalCount = healthAware.getTotalCount();
            unavailableCount = healthAware.getUnavailableCount();
            unhealthyCount = healthAware.getUnhealthyCount();
            degradedCount = healthAware.getDegradedCount();
        }

        @Override
        public LocalDateTime getCreatedAt() {
            return TimeUtils.toLocalDateTime(timestamp);
        }

        int getCount(Health health) {
            return switch (health) {
                case DEGRADED -> degradedCount;
                case UNHEALTHY -> unhealthyCount;
                case UNAVAILABLE -> unavailableCount;
                default -> 0;
            };
        }


    }
}
