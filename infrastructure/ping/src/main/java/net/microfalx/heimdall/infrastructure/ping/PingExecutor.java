package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;

import java.time.Duration;
import java.time.ZonedDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which performs the actual ping.
 */
class PingExecutor implements net.microfalx.heimdall.infrastructure.api.Ping {

    private final Service service;
    private final Server server;
    private final PingPersistence persistence;

    PingExecutor(Service service, Server server, PingPersistence persistence) {
        requireNonNull(service);
        requireNonNull(server);
        requireNonNull(persistence);
        this.service = service;
        this.server = server;
        this.persistence = persistence;
    }

    net.microfalx.heimdall.infrastructure.api.Ping execute() {
        return this;
    }

    @Override
    public Service getService() {
        return service;
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public ZonedDateTime getStartedAt() {
        return null;
    }

    @Override
    public ZonedDateTime getEndedAt() {
        return null;
    }

    @Override
    public Duration getDuration() {
        return null;
    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return "";
    }
}
