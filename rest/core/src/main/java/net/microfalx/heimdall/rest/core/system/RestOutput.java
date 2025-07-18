package net.microfalx.heimdall.rest.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.surrogate.IdentityAware;
import net.microfalx.heimdall.infrastructure.core.system.Environment;
import net.microfalx.heimdall.rest.api.Status;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rest_output")
@Name("Outputs")
@ReadOnly
@ToString
@Getter
@Setter
public class RestOutput extends IdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false)
    @Description("The environment targeted by a simulation")
    @Name
    @Position(10)
    private Environment environment;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    @Description("The simulation used to produce the output")
    @Name
    @Position(15)
    private RestSimulation simulation;

    @OneToOne
    @JoinColumn(name = "result_id", nullable = false)
    @Description("The simulation result")
    @Position(16)
    private RestResult result;

    @ManyToOne
    @JoinColumn(name = "scenario_id", nullable = false)
    @Description("The status of the simulation result")
    @Position(17)
    private RestScenario scenario;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The status of the simulation result")
    @Position(18)
    private Status status;

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
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private int duration;

    @Column(name = "apdex", nullable = false)
    @Description("The APDEX score of the scenario")
    @Position(32)
    @Formattable()
    private float apdex;

    @Column(name = "data_received", nullable = false)
    @Description("The amount of received data")
    @Position(35)
    @Visible(false)
    private float dataReceived;

    @Column(name = "data_sent", nullable = false)
    @Description("the amount of data sent")
    @Position(35)
    @Visible(false)
    private float dataSent;

    @Label(group = "Iterations", value = "Count")
    @Column(name = "iterations", nullable = false)
    @Description("the aggregate number of times the VUs execute the script")
    @Position(40)
    private float iterations;

    @Label(group = "Iterations", value = "Duration")
    @Column(name = "iteration_duration", nullable = false)
    @Description("The time to complete one full iteration, including time spent in setup and teardown")
    @Position(45)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private float iterationDuration;

    @Label(group = "Users", value = "Active")
    @Column(name = "vus", nullable = false)
    @Description("The current number of active virtual users")
    @Position(50)
    private float vus;

    @Label(group = "Users", value = "Maximum")
    @Column(name = "vus_max", nullable = false)
    @Description("The maximum possible number of virtual users")
    @Position(55)
    private float vusMax;

    @Column(name = "http_request_blocked", nullable = false)
    @Description("The time spent blocked (waiting for a free TCP connection slot) before initiating the request")
    @Position(60)
    @Visible(false)
    private float httpRequestBlocked;

    @Column(name = "http_request_connecting", nullable = false)
    @Description("The time spent establishing TCP connection to the remote host")
    @Position(65)
    @Visible(false)
    private float httpRequestConnecting;

    @Column(name = "http_Request_duration", nullable = false)
    @Description("Returns the total time for the request")
    @Position(70)
    @Visible(false)
    private float httpRequestDuration;

    @Label(group = "Failures", value = "Total")
    @Column(name = "http_request_failed", nullable = false)
    @Description("The rate of failed requests")
    @Position(75)
    private float httpRequestFailed;

    @Label(group = "Failures", value = "4xx")
    @Column(name = "http_request_failed_4xx", nullable = false)
    @Description("The rate of failed requests with errors in 4XX range")
    @Position(76)
    private float httpRequestFailed4XX;

    @Label(group = "Failures", value = "5xx")
    @Column(name = "http_request_failed_5xx", nullable = false)
    @Description("The rate of failed requests with errors in 5XX range")
    @Position(77)
    private float httpRequestFailed5XX;

    @Column(name = "http_request_receiving", nullable = false)
    @Description("The time spent receiving response data from the remote host")
    @Position(80)
    @Visible(false)
    private float httpRequestReceiving;

    @Column(name = "http_request_sending", nullable = false)
    @Description("The time spent receiving response data from the remote host")
    @Position(85)
    @Visible(false)
    private float httpRequestSending;

    @Column(name = "http_request_tls_handshaking", nullable = false)
    @Description("The time spent handshaking TLS session with remote host")
    @Position(90)
    @Visible(false)
    private float httpRequestTlsHandshaking;

    @Column(name = "http_request_waiting", nullable = false)
    @Description("The time spent waiting for response from remote host (a.k.a. “time to first byte”, or “TTFB”)")
    @Position(95)
    @Visible(false)
    private float httpRequestWaiting;

    @Column(name = "http_requests", nullable = false)
    @Description("The total amount of HTTP requests generated")
    @Position(100)
    @Visible(false)
    private float httpRequests;
}
