package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.lang.UriUtils;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.time.ZonedDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;
import static net.microfalx.lang.UriUtils.isValidUri;
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

    private Status status = Status.SUCCESS;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private Integer errorCode;
    private String errorMessage;

    PingExecutor(Ping ping, Service service, Server server, PingPersistence persistence) {
        requireNonNull(ping);
        requireNonNull(service);
        requireNonNull(server);
        requireNonNull(persistence);
        this.ping = ping;
        this.persistence = persistence;
        this.service = service;
        this.server = server;
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
        switch (ping.getService().getType()) {
            case ICMP -> doPingIcmp();
            case HTTP, HTTPS -> doPingHttpAndHttps();
            case TCP -> doPingTcp();
            case UDP -> doPingUdp();
        }

    }

    private void doPingIcmp() throws IOException {
        if (ping.getServer().isIcmp()) {
            boolean isServerPing = InetAddress.getByName(ping.getServer().getHostname())
                    .isReachable(ping.getService().getConnectionTimeOut());
            if (!isServerPing) status = Status.FAILURE;
        } else {
            status = Status.CANCEL;
        }
    }

    private void doPingHttpAndHttps() throws MalformedURLException {
        if (isValidUri(service.getPath())) {
            try {
                HttpURLConnection con = (HttpURLConnection) UriUtils.parseUrl(service.getPath()).openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(ping.getConnectionTimeOut());
                con.setReadTimeout(ping.getReadTimeOut());
                con.connect();
                int responseCode = con.getResponseCode();
                if (!(responseCode == HttpURLConnection.HTTP_OK)) {
                    status = Status.FAILURE;
                    errorCode = responseCode;
                }
            } catch (SocketTimeoutException e) {
                status = Status.TIMEOUT;
            } catch (IOException e) {
                status = Status.FAILURE;
                errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
            }
        } else {
            throw new MalformedURLException("Something is wrong with the path");
        }
    }

    private void doPingTcp() {
        try (Socket socket = new Socket(InetAddress.getByName(null).getHostName(),
                service.getType().getPort())) {
            socket.connect(new InetSocketAddress(service.getPort()), ping.getConnectionTimeOut());
        } catch (SocketTimeoutException e) {
            status = Status.TIMEOUT;
        } catch (IOException e) {
            status = Status.FAILURE;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        }
    }

    private void doPingUdp() {
        try (DatagramSocket socket = new DatagramSocket(service.getType().getPort())) {
            socket.connect(new InetSocketAddress(service.getType().getPort()));
            socket.setSoTimeout(ping.getConnectionTimeOut());
        } catch (IOException e) {
            status = Status.FAILURE;
            errorMessage = abbreviate(e.getMessage(), MAX_MESSAGE_WIDTH);
        }
    }

    private void doPersistPing() {
        persistence.persist(ping, this);
    }
}
