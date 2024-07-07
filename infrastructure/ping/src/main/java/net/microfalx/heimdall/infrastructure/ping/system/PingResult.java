package net.microfalx.heimdall.infrastructure.ping.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.DurationConverter;
import net.microfalx.heimdall.infrastructure.api.Status;
import net.microfalx.heimdall.infrastructure.core.system.Server;
import net.microfalx.heimdall.infrastructure.core.system.Service;
import net.microfalx.heimdall.infrastructure.ping.dataset.StatusAlertProvider;
import net.microfalx.lang.annotation.*;

import java.time.Duration;
import java.time.ZonedDateTime;

@Entity
@ReadOnly
@Table(name = "infrastructure_ping_result")
@Getter
@Setter
@ToString(callSuper = true)
@Name("Health Check Results")
public class PingResult extends IdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "ping_id")
    @Name
    @Label("Name")
    @Position(1)
    private Ping ping;

    @ManyToOne
    @JoinColumn(name = "server_id")
    @Position(2)
    private Server server;

    @ManyToOne
    @JoinColumn(name = "service_id")
    @Position(3)
    private Service service;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Formattable(alert = StatusAlertProvider.class)
    @Description("The status of the ping")
    @Position(10)
    private Status status;

    @Column(name = "started_at", nullable = false)
    @Position(30)
    @net.microfalx.bootstrap.dataset.annotation.OrderBy(OrderBy.Direction.DESC)
    @Timestamp
    private ZonedDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    @Position(31)
    private ZonedDateTime endedAt;

    @Column(name = "duration", nullable = false)
    @Convert(converter = DurationConverter.class)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Position(32)
    @Width("90")
    private Duration duration;

    @Column(name = "error_code")
    @Description("The status of the ping for an application service")
    @Position(40)
    @Width("100")
    private Integer errorCode;

    @Column(name = "error_message")
    @Description("The error message for the ping")
    @Position(41)
    @Width("300")
    private String errorMessage;

}
