package net.microfalx.heimdall.rest.core.overview;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.infrastructure.core.system.Environment;
import net.microfalx.heimdall.rest.core.system.Simulation;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "rest_output")
@ToString
@Getter
@Setter
public class Output {

    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @JoinColumn(name = "simulation_id", nullable = false)
    private Simulation simulation;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Column(name = "duration", nullable = false)
    private Duration duration;

    @Column(name = "data_received", nullable = false)
    private Float dataReceived;

    @Column(name = "data_sent", nullable = false)
    private Float dataSent;

    @Column(name = "iterations", nullable = false)
    private Float iterations;

    @Column(name = "iteration_duration", nullable = false)
    private Float iterationDuration;

    @Column(name = "vus", nullable = false)
    private Float vus;

    @Column(name = "vus_max", nullable = false)
    private Float vusMax;

    @Column(name = "http_request_blocked", nullable = false)
    private Float httpRequestBlocked;

    @Column(name = "http_request_connecting", nullable = false)
    private Float httpRequestConnecting;

    @Column(name = "http_Request_duration", nullable = false)
    private Float httpRequestDuration;

    @Column(name = "http_request_failed", nullable = false)
    private Float httpRequestFailed;

    @Column(name = "http_request_receiving", nullable = false)
    private Float httpRequestReceiving;

    @Column(name = "http_request_sending", nullable = false)
    private Float httpRequestSending;

    @Column(name = "http_request_tls_handshaking", nullable = false)
    private Float httpRequestTlsHandshaking;

    @Column(name = "http_request_waiting", nullable = false)
    private Float httpRequestWaiting;

    @Column(name = "http_requests", nullable = false)
    private Float httpRequests;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "description", length = 1000)
    private String description;
}
