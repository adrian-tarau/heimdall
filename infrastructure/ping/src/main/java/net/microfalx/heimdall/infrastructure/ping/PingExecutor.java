package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.lang.ObjectUtils;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Base64;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static org.apache.commons.lang3.StringUtils.abbreviate;

/**
 * A class which performs the actual ping.
 */
class PingExecutor implements net.microfalx.heimdall.infrastructure.api.Ping {

    public static final int MAX_MESSAGE_WIDTH = 1000;
    private final Ping ping;
    private final Service service;
    private final Server server;
    private final PingPersistence persistence;
    private final InfrastructureService infrastructureService;

    private Status status = Status.SUCCESS;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private Integer errorCode;
    private String errorMessage;

    PingExecutor(Ping ping, Service service, Server server, PingPersistence persistence,
                 InfrastructureService infrastructureService) {
        requireNonNull(ping);
        requireNonNull(service);
        requireNonNull(server);
        requireNonNull(persistence);
        requireNonNull(infrastructureService);
        this.ping = ping;
        this.persistence = persistence;
        this.service = service;
        this.server = server;
        this.infrastructureService = infrastructureService;
    }

    net.microfalx.heimdall.infrastructure.api.Ping execute() {
        start = ZonedDateTime.now();
        try {
            doPing();
        } catch (Exception e) {
            status = Status.FAILURE;
            errorMessage = getRootCauseMessage(e);
        } finally {
            end = ZonedDateTime.now();
        }
        doPersistPing();
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
        return start;
    }

    @Override
    public ZonedDateTime getEndedAt() {
        return end;
    }

    @Override
    public Duration getDuration() {
        return Duration.between(getEndedAt(), getStartedAt());
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    private void doPing() throws IOException {
        switch (service.getType()) {
            case ICMP -> doPingIcmp();
            case HTTP, HTTPS -> doPingHttpAndHttps();
            case TCP -> doPingTcp();
            case UDP -> doPingUdp();
        }

    }

    private void doPingIcmp() throws IOException {
        if (server.isIcmp()) {
            boolean isServerPing = InetAddress.getByName(server.getHostname())
                    .isReachable((int) service.getConnectionTimeout().toMillis());
            if (!isServerPing) status = Status.FAILURE;
        } else {
            status = Status.CANCEL;
        }
    }

    private void doPingHttpAndHttps() {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(createHttpUri())
                    .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(getReadTimeout())).GET();
            updateAuthentication(builder);
            HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.getDefault())
                    .connectTimeout(Duration.ofMillis(getConnectionTimeout()))
                    .followRedirects(HttpClient.Redirect.ALWAYS).build();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) errorCode = response.statusCode();
        } catch (HttpTimeoutException e) {
            status = Status.TIMEOUT;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        } catch (Exception e) {
            status = Status.FAILURE;
            errorCode=500;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        }
    }

    private void updateAuthentication(HttpRequest.Builder builder) {
        String valueToEncode = service.getUserName() + ":" + service.getPassword();
        if (service.getAuthType() == Service.AuthType.BASIC) {
            builder.header("Authorization", "Basic " + Base64.getEncoder().
                    encodeToString(valueToEncode.getBytes()));
        } else if (service.getAuthType() == Service.AuthType.BEARER) {
            builder.header("Authorization", service.getToken());
        } else if (service.getAuthType() == Service.AuthType.API_KEY) {
            builder.header("X-API-KEY", service.getToken());
        }

    }

    private URI createHttpUri() {
        Environment environment = infrastructureService.find(server).stream().findFirst().orElse(null);
        return service.getUri(server, environment);
    }

    private void doPingTcp() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByName(server.getHostname()), service.getPort()), getConnectionTimeout());
        } catch (SocketTimeoutException e) {
            status = Status.TIMEOUT;
        } catch (Exception e) {
            status = Status.FAILURE;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        }
    }

    private void doPingUdp() {
        try (DatagramSocket socket = new DatagramSocket(service.getPort())) {
            socket.connect(new InetSocketAddress(service.getPort()));
            socket.setSoTimeout(ping.getConnectionTimeOut());
        } catch (Exception e) {
            status = Status.FAILURE;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        }
    }

    private void doPersistPing() {
        persistence.persist(ping, this);
    }

    private int getConnectionTimeout() {
        return ObjectUtils.defaultIfNull(ping.getConnectionTimeOut(), service.getConnectionTimeout().toMillis()).intValue();
    }

    private int getReadTimeout() {
        return ObjectUtils.defaultIfNull(ping.getReadTimeOut(), service.getReadTimeout().toMillis()).intValue();
    }
}