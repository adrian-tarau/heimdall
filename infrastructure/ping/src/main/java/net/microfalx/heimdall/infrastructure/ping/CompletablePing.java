package net.microfalx.heimdall.infrastructure.ping;

import lombok.ToString;
import net.microfalx.heimdall.infrastructure.api.Ping;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.api.Status;

import java.time.Duration;
import java.time.ZonedDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@ToString
public class CompletablePing implements Ping {

    private final String id;
    private final Service service;
    private final Server server;

    private Status status = Status.NA;
    private ZonedDateTime startedAt = ZonedDateTime.now();
    private ZonedDateTime endedAt = startedAt;
    private Integer errorCode;
    private String errorMessage;

    public CompletablePing(Service service, Server server) {
        this.service = service;
        this.server = server;
        this.id = PingUtils.getId(service, server);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return PingUtils.getName(service, server);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        requireNonNull(status);
        this.status = status;
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
        return startedAt;
    }

    public void setStartedAt(ZonedDateTime startedAt) {
        requireNonNull(startedAt);
        this.startedAt = startedAt;
    }

    @Override
    public ZonedDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(ZonedDateTime endedAt) {
        requireNonNull(endedAt);
        this.endedAt = endedAt;
    }

    @Override
    public Duration getDuration() {
        return Duration.between(getStartedAt(), getEndedAt());
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
