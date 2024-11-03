package net.microfalx.heimdall.rest.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.metrics.Matrix;
import net.microfalx.bootstrap.metrics.Vector;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.rest.api.Metrics;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Simulation;

import java.time.LocalDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class SimulationOutput implements Output {

    @ToString.Include
    private final String id;
    @ToString.Include
    private final String name;
    @ToString.Include
    private final Environment environment;
    @ToString.Include
    private final Simulation simulation;

    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime = startTime;

    private Vector dataReceived;
    private Vector dataSent;

    private Vector iterations;
    private Matrix iterationDuration;

    private Vector vus;
    private Vector vusMax;

    private Matrix httpRequestBlocked;
    private Matrix httpRequestConnecting;
    private Matrix httpRequestDuration;
    private Vector httpRequestFailed;

    private Matrix httpRequestReceiving;
    private Matrix httpRequestSending;
    private Matrix httpRequestTlsHandshaking;
    private Matrix httpRequestWaiting;
    private Vector httpRequests;

    public SimulationOutput(String id, String name, Environment environment, Simulation simulation) {
        requireNonNull(id);
        requireNonNull(name);
        requireNonNull(environment);
        requireNonNull(simulation);
        this.id = toIdentifier(id);
        this.name = name;
        this.environment = environment;
        this.simulation = simulation;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Vector getDataReceived() {
        return dataReceived != null ? dataReceived : Vector.empty(Metrics.DATA_RECEIVED);
    }

    public Vector getDataSent() {
        return dataSent != null ? dataSent : Vector.empty(Metrics.DATA_SENT);
    }

    public Vector getIterations() {
        return iterations != null ? iterations : Vector.empty(Metrics.ITERATIONS);
    }

    public Matrix getIterationDuration() {
        return iterationDuration != null ? iterationDuration : Matrix.empty(Metrics.ITERATION_DURATION);
    }

    public Vector getVus() {
        return vus != null ? vus : Vector.empty(Metrics.VUS);
    }

    public Vector getVusMax() {
        return vusMax != null ? vusMax : Vector.empty(Metrics.VUS_MAX);
    }

    public Matrix getHttpRequestBlocked() {
        return httpRequestBlocked != null ? httpRequestBlocked : Matrix.empty(Metrics.HTTP_REQ_BLOCKED);
    }

    public Matrix getHttpRequestConnecting() {
        return httpRequestConnecting != null ? httpRequestConnecting : Matrix.empty(Metrics.HTTP_REQ_CONNECTING);
    }

    public Matrix getHttpRequestDuration() {
        return httpRequestDuration != null ? httpRequestDuration : Matrix.empty(Metrics.HTTP_REQ_DURATION);
    }

    public Vector getHttpRequestFailed() {
        return httpRequestFailed != null ? httpRequestFailed : Vector.empty(Metrics.HTTP_REQ_FAILED);
    }

    public Matrix getHttpRequestReceiving() {
        return httpRequestReceiving != null ? httpRequestReceiving : Matrix.empty(Metrics.HTTP_REQ_RECEIVING);
    }

    public Matrix getHttpRequestSending() {
        return httpRequestSending != null ? httpRequestSending : Matrix.empty(Metrics.HTTP_REQ_SENDING);
    }

    public Matrix getHttpRequestTlsHandshaking() {
        return httpRequestTlsHandshaking != null ? httpRequestTlsHandshaking : Matrix.empty(Metrics.HTTP_REQ_TLS_HANDSHAKING);
    }

    public Matrix getHttpRequestWaiting() {
        return httpRequestWaiting != null ? httpRequestWaiting : Matrix.empty(Metrics.HTTP_REQ_WAITING);
    }

    public Vector getHttpRequests() {
        return httpRequests != null ? httpRequests : Vector.empty(Metrics.HTTP_REQS);
    }
}
