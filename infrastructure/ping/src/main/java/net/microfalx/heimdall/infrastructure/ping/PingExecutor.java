package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.*;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
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
    private final String id;
    private final net.microfalx.heimdall.infrastructure.ping.system.Ping ping;
    private final Service service;
    private final Server server;
    private final PingPersistence persistence;
    private final InfrastructureService infrastructureService;
    private final PingHealth health;

    private boolean persist = true;
    private boolean useLiveness;
    private Status status = Status.NA;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private Integer errorCode;
    private String errorMessage;

    PingExecutor(net.microfalx.heimdall.infrastructure.ping.system.Ping ping, Service service, Server server, PingPersistence persistence,
                 InfrastructureService infrastructureService, PingHealth health) {
        requireNonNull(service);
        requireNonNull(server);
        requireNonNull(persistence);
        requireNonNull(infrastructureService);
        requireNonNull(health);
        this.id = PingUtils.getId(service, server);
        this.ping = ping;
        this.persistence = persistence;
        this.service = service;
        this.server = server;
        this.infrastructureService = infrastructureService;
        this.health = health;
    }

    public PingExecutor setPersist(boolean persist) {
        this.persist = persist;
        return this;
    }

    public PingExecutor setUseLiveness(boolean useLiveness) {
        this.useLiveness = useLiveness;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        if (ping == null) {
            return server.getName() + "(" + service.getName() + ")";
        } else {
            return ping.getName();
        }
    }

    @Override
    public String getDescription() {
        if (ping == null) {
            return null;
        } else {
            return ping.getName();
        }
    }

    net.microfalx.heimdall.infrastructure.ping.system.Ping getPing() {
        if (ping == null) throw new IllegalStateException("A ping is not available");
        return ping;
    }

    net.microfalx.heimdall.infrastructure.api.Ping execute() {
        start = ZonedDateTime.now();
        try {
            doPing();
        } catch (Exception e) {
            status = Status.L7STS;
            errorMessage = getRootCauseMessage(e);
        } finally {
            end = ZonedDateTime.now();
        }
        if (persist) {
            health.registerPing(this);
            doPersistPing();
        }
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
        return Duration.between(getStartedAt(), getEndedAt());
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
            case HTTP -> doPingHttp();
            case TCP -> doPingTcp();
            case UDP -> doPingUdp();
        }

    }

    private void doPingIcmp() throws IOException {
        if (server.isIcmp()) {
            boolean isServerPing = InetAddress.getByName(server.getHostname())
                    .isReachable((int) service.getConnectionTimeout().toMillis());
            status = isServerPing ? Status.L3OK : Status.L3CON;
        } else {
            status = Status.NA;
        }
    }

    private void doPingHttp() {
        Status baseStatus = doPingHttp(createHttpBaseUri());
        Integer baseErrorCode = this.errorCode;
        String baseErrorMessage = this.errorMessage;

        Status livenessStatus = null;
        Integer livenessErrorCode = null;
        String livenessErrorMessage = null;
        if (service.getLivenessPath() != null) {
            livenessStatus = doPingHttp(createHttpLivenessUri());
            livenessErrorCode = this.errorCode;
            livenessErrorMessage = this.errorMessage;
        }

        Status readinessStatus = null;
        Integer readinessErrorCode = null;
        String readinessErrorMessage = null;
        if (service.getReadinessPath() != null) {
            readinessStatus = doPingHttp(createHttpReadinessUri());
            readinessErrorCode = this.errorCode;
            readinessErrorMessage = this.errorMessage;
        }
        if (baseStatus.isFailure() || baseStatus == Status.L7DEN) {
            this.status = baseStatus;
            this.errorCode = baseErrorCode;
            this.errorMessage = baseErrorMessage;
            if (useLiveness && baseStatus == Status.L7DEN && livenessStatus != null && !livenessStatus.isFailure()) {
                this.status = livenessStatus;
                this.errorCode = livenessErrorCode;
                this.errorMessage = livenessErrorMessage;
            }
        } else if (livenessStatus != null && livenessStatus.isFailure()) {
            this.status = livenessStatus;
            this.errorCode = livenessErrorCode;
            this.errorMessage = livenessErrorMessage;
        } else if (readinessStatus != null && readinessStatus.isFailure()) {
            this.status = readinessStatus;
            this.errorCode = readinessErrorCode;
            this.errorMessage = readinessErrorMessage;
        } else {
            this.status = baseStatus;
            this.errorCode = baseErrorCode;
            this.errorMessage = baseErrorMessage;
        }
    }

    private Status doPingHttp(URI uri) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri)
                    .version(HttpClient.Version.HTTP_2).timeout(Duration.ofMillis(getReadTimeout())).GET();
            updateAuthentication(builder);
            HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.getDefault())
                    .connectTimeout(Duration.ofMillis(getConnectionTimeout()))
                    .followRedirects(HttpClient.Redirect.ALWAYS).build();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500) {
                errorCode = response.statusCode();
                if (response.statusCode() == 503) {
                    status = Status.L7TOUT;
                } else {
                    status = Status.L7STS;
                }
            } else if (response.statusCode() >= 400) {
                errorCode = response.statusCode();
                status = isSecurityErrorCode(errorCode) ? Status.L7DEN : Status.L7STS;
            } else {
                status = Status.L7OK;
            }
        } catch (HttpConnectTimeoutException e) {
            status = Status.L4TOUT;
        } catch (HttpTimeoutException e) {
            status = Status.L7TOUT;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        } catch (ConnectException e) {
            status = Status.L4CON;
        } catch (SSLException e) {
            status = Status.L4CON;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        } catch (Exception e) {
            status = Status.L7STS;
            errorCode = 500;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        }
        return status;
    }

    private void updateAuthentication(HttpRequest.Builder builder) {
        String valueToEncode = service.getUserName() + ":" + service.getPassword();
        if (service.getAuthType() == Service.AuthType.BASIC) {
            builder.header("Authorization", "Basic " + Base64.getEncoder().
                    encodeToString(valueToEncode.getBytes()));
        } else if (service.getAuthType() == Service.AuthType.BEARER) {
            builder.header("Authorization", "Bearer " + service.getToken());
        } else if (service.getAuthType() == Service.AuthType.API_KEY) {
            builder.header("X-API-KEY", service.getToken());
        }

    }

    private URI createHttpBaseUri() {
        Environment environment = infrastructureService.find(server).stream().findFirst().orElse(null);
        return service.getUri(server, environment);
    }

    private URI createHttpLivenessUri() {
        Environment environment = infrastructureService.find(server).stream().findFirst().orElse(null);
        return service.getLivenessUri(server, environment);
    }

    private URI createHttpReadinessUri() {
        Environment environment = infrastructureService.find(server).stream().findFirst().orElse(null);
        return service.getReadinessUri(server, environment);
    }

    private boolean isSecurityErrorCode(Integer errorCode) {
        return errorCode != null && (errorCode == 401 || errorCode == 403);
    }

    private void doPingTcp() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByName(server.getHostname()), service.getPort()),
                    getConnectionTimeout());
            status = Status.L4OK;
        } catch (SocketTimeoutException e) {
            status = Status.L4TOUT;
        } catch (Exception e) {
            status = Status.L4CON;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        }
    }

    private void doPingUdp() {
        try (DatagramSocket socket = new DatagramSocket(service.getPort())) {
            socket.connect(new InetSocketAddress(service.getPort()));
            socket.setSoTimeout(getConnectionTimeout());
            status = Status.L4OK;
        } catch (Exception e) {
            status = Status.L4CON;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        }
    }

    private void doPersistPing() {
        if (ping != null) persistence.persist(ping, this);
    }

    private int getConnectionTimeout() {
        if (ping != null && ping.getConnectionTimeOut() != null) {
            return ping.getConnectionTimeOut();
        } else {
            return (int) service.getConnectionTimeout().toMillis();
        }
    }

    private int getReadTimeout() {
        if (ping != null && ping.getConnectionTimeOut() != null) {
            return ping.getReadTimeOut();
        } else {
            return (int) service.getReadTimeout().toMillis();
        }
    }
}
