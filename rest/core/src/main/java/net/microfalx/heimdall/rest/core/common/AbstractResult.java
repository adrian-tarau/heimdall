package net.microfalx.heimdall.rest.core.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.annotation.*;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.infrastructure.core.system.Environment;
import net.microfalx.heimdall.rest.api.Status;
import net.microfalx.heimdall.rest.core.system.RestResult;
import net.microfalx.heimdall.rest.core.system.RestSimulation;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@MappedSuperclass
@ReadOnly
@ToString
@Getter
@Setter
public abstract class AbstractResult extends IdentityAware<Long> {

    @ManyToOne
    @JoinColumn(name = "environment_id", nullable = false)
    @Description("The environment")
    @Name
    @Position(5)
    private Environment environment;

    @ManyToOne
    @JoinColumn(name = "simulation_id", nullable = false)
    @Description("The simulation")
    @Name
    @Position(10)
    private RestSimulation simulation;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The status of the simulation")
    @Position(15)
    @Formattable(alert = RestResult.AlertProvider.class)
    private Status status;

    @Description("The start time of the simulation")
    @Position(20)
    @Timestamp
    @CreatedAt
    @net.microfalx.bootstrap.dataset.annotation.OrderBy(OrderBy.Direction.DESC)
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

    @Column(name = "data_received")
    @Description("The amount of received data")
    @Position(35)
    @Visible(false)
    private Float dataReceived;

    @Column(name = "data_sent")
    @Description("the amount of data sent")
    @Position(35)
    @Visible(false)
    private Float dataSent;

    @Label(group = "Iterations", value = "Count")
    @Column(name = "iterations")
    @Description("the aggregate number of times the VUs execute the script")
    @Position(40)
    private Float iterations;

    @Label(group = "Iterations", value = "Duration")
    @Column(name = "iteration_duration")
    @Description("The time to complete one full iteration, including time spent in setup and teardown")
    @Position(45)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Float iterationDuration;

    @Label(group = "Users", value = "Active")
    @Column(name = "vus")
    @Description("The number of active virtual users")
    @Position(50)
    private Float vus;

    @Label(group = "Users", value = "Maximum")
    @Column(name = "vus_max")
    @Description("The maximum possible number of virtual users")
    @Position(55)
    private Float vusMax;

    @Column(name = "http_request_blocked")
    @Description("The time spent blocked (waiting for a free TCP connection slot) before initiating the request")
    @Position(60)
    @Visible(false)
    private Float httpRequestBlocked;

    @Column(name = "http_request_connecting")
    @Description("The time spent establishing TCP connection to the remote host")
    @Position(65)
    @Visible(false)
    private Float httpRequestConnecting;

    @Column(name = "http_Request_duration")
    @Description("Returns the total time for the request")
    @Position(70)
    @Visible(false)
    private Float httpRequestDuration;

    @Column(name = "http_request_failed")
    @Description("The rate of failed requests")
    @Position(75)
    @Visible(false)
    private Float httpRequestFailed;

    @Column(name = "http_request_receiving")
    @Description("The time spent receiving response data from the remote host")
    @Position(80)
    @Visible(false)
    private Float httpRequestReceiving;

    @Column(name = "http_request_sending")
    @Description("The time spent receiving response data from the remote host")
    @Position(85)
    @Visible(false)
    private Float httpRequestSending;

    @Column(name = "http_request_tls_handshaking")
    @Description("The time spent handshaking TLS session with remote host")
    @Position(90)
    @Visible(false)
    private Float httpRequestTlsHandshaking;

    @Column(name = "http_request_waiting")
    @Description("The time spent waiting for response from remote host (a.k.a. “time to first byte”, or “TTFB”)")
    @Position(95)
    @Visible(false)
    private Float httpRequestWaiting;

    @Column(name = "http_requests")
    @Description("The total amount of HTTP requests generated")
    @Position(100)
    @Visible(false)
    private Float httpRequests;

    @Label("Logs")
    @Column(name = "logs_uri", length = 500)
    @Description("A resource containing the log of the simulation")
    @Align(Align.Type.CENTER)
    @Position(105)
    @Renderable(action = "rest.simulation.view.logs", icon = "fa-regular fa-file-lines")
    private String logsURI;

    @Label("Report")
    @Column(name = "report_uri", length = 500)
    @Description("The resource containing the report of the simulation")
    @Align(Align.Type.CENTER)
    @Position(110)
    @Renderable(action = "rest.simulation.view.report", icon = "fa-solid fa-chart-line")
    private String reportURI;

    @Label("Data")
    @Column(name = "data_uri", length = 500)
    @Description("The resource containing the data produced byt the simulation")
    @Align(Align.Type.CENTER)
    @Position(115)
    @Renderable(action = "rest.simulation.view.data", icon = "fa-solid fa-file-csv")
    private String dataURI;

    @Column(name = "version", length = 50)
    @Description("The version of the software available in the environment at the time of the simulation")
    @Position(200)
    private String version;

    @Column(name = "description")
    @Position(1000)
    @Component(Component.Type.TEXT_AREA)
    @Description("A description for a {name}")
    @Width("300px")
    @Visible(false)
    @Filterable()
    private String description;

    @Column(name = "error_message")
    @Visible(false)
    @Width("300px")
    private String errorMessage;


    public static class AlertProvider implements Formattable.AlertProvider<AbstractResult, Field<AbstractResult>, Status> {

        @Override
        public Alert provide(Status value, Field<AbstractResult> field, AbstractResult model) {
            Alert.Type type = switch (value) {
                case SUCCESSFUL -> Alert.Type.SUCCESS;
                case FAILED -> Alert.Type.DANGER;
                case CANCELED -> Alert.Type.DARK;
                case UNKNOWN -> Alert.Type.LIGHT;
            };
            return Alert.builder().type(type).message(model.getErrorMessage()).build();
        }

    }
}