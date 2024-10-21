package net.microfalx.heimdall.rest.core.overview;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.heimdall.infrastructure.core.system.Environment;
import net.microfalx.heimdall.rest.core.system.Simulation;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "rest_output")
@ToString
@Getter
@Setter
public class Output extends NamedAndTimestampedIdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false)
    @Description("The environment targeted by a simulation")
    @Position(10)
    private Environment environment;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    @Description("The simulation used to produce the output")
    @Position(15)
    private Simulation simulation;

    @Column(name = "started_at", nullable = false)
    @Description("the start time of the simulation")
    @Position(20)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    @Description("The end time of the simulation")
    @Position(25)
    private LocalDateTime endedAt;

    @Column(name = "duration", nullable = false)
    @Description("The duration of the output")
    @Position(30)
    private Duration duration;

    @Column(name = "data_received", nullable = false)
    @Description("The amount of received data")
    @Position(35)
    private Float dataReceived;

    @Column(name = "data_sent", nullable = false)
    @Description("the amount of data sent")
    @Position(35)
    private Float dataSent;

    @Column(name = "iterations", nullable = false)
    @Description("the aggregate number of times the VUs execute the script")
    @Position(40)
    private Float iterations;

    @Column(name = "iteration_duration", nullable = false)
    @Description("The time to complete one full iteration, including time spent in setup and teardown")
    @Position(45)
    private Float iterationDuration;

    @Column(name = "vus", nullable = false)
    @Description("The current number of active virtual users")
    @Position(50)
    private Float vus;

    @Column(name = "vus_max", nullable = false)
    @Description("The maximum possible number of virtual users")
    @Position(55)
    private Float vusMax;

    @Column(name = "http_request_blocked", nullable = false)
    @Description("The time spent blocked (waiting for a free TCP connection slot) before initiating the request")
    @Position(60)
    private Float httpRequestBlocked;

    @Column(name = "http_request_connecting", nullable = false)
    @Description("The time spent establishing TCP connection to the remote host")
    @Position(65)
    private Float httpRequestConnecting;

    @Column(name = "http_Request_duration", nullable = false)
    @Description("Returns the total time for the request")
    @Position(70)
    private Float httpRequestDuration;

    @Column(name = "http_request_failed", nullable = false)
    @Description("The rate of failed requests")
    @Position(75)
    private Float httpRequestFailed;

    @Column(name = "http_request_receiving", nullable = false)
    @Description("The time spent receiving response data from the remote host")
    @Position(80)
    private Float httpRequestReceiving;

    @Column(name = "http_request_sending", nullable = false)
    @Description("The time spent receiving response data from the remote host")
    @Position(85)
    private Float httpRequestSending;

    @Column(name = "http_request_tls_handshaking", nullable = false)
    @Description("The time spent handshaking TLS session with remote host")
    @Position(90)
    private Float httpRequestTlsHandshaking;

    @Column(name = "http_request_waiting", nullable = false)
    @Description("The time spent waiting for response from remote host (a.k.a. “time to first byte”, or “TTFB”)")
    @Position(95)
    private Float httpRequestWaiting;

    @Column(name = "http_requests", nullable = false)
    @Description("The total amount of HTTP requests generated")
    @Position(100)
    private Float httpRequests;

    @Column(name = "version", length = 50)
    @Description("The version")
    private String version;
}
