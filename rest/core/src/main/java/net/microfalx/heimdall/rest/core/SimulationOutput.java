package net.microfalx.heimdall.rest.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.metrics.Matrix;
import net.microfalx.bootstrap.metrics.Vector;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.rest.api.Metrics;
import net.microfalx.heimdall.rest.api.Output;
import net.microfalx.heimdall.rest.api.Scenario;
import net.microfalx.heimdall.rest.api.Simulation;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class SimulationOutput implements Output {

    @ToString.Include
    private final Scenario scenario;
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

    private Matrix vus;
    private Matrix vusMax;

    private Matrix httpRequestBlocked;
    private Matrix httpRequestConnecting;
    private Matrix httpRequestDuration;
    private Vector httpRequestFailed;

    private Matrix httpRequestReceiving;
    private Matrix httpRequestSending;
    private Matrix httpRequestTlsHandshaking;
    private Matrix httpRequestWaiting;
    private Vector httpRequests;

    public SimulationOutput(Scenario scenario, Environment environment, Simulation simulation) {
        requireNonNull(scenario);
        requireNonNull(environment);
        requireNonNull(simulation);
        this.scenario = scenario;
        this.environment = environment;
        this.simulation = simulation;
    }

    public String getId() {
        return scenario.getId();
    }

    public String getName() {
        return scenario.getName();
    }

    @Override
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
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

    public Matrix getVus() {
        return vus != null ? vus : Matrix.empty(Metrics.VUS);
    }

    public Matrix getVusMax() {
        return vusMax != null ? vusMax : Matrix.empty(Metrics.VUS_MAX);
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

    @Override
    public float getApdex() {
        return RestUtils.getApdexScore(scenario, getIterationDuration().getValues());
    }
}
