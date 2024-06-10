package net.microfalx.heimdall.infrastructure.ping;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.heimdall.infrastructure.core.Server;
import net.microfalx.heimdall.infrastructure.core.Service;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;

import java.time.Duration;
import java.time.ZonedDateTime;

@Entity
@Table(name = "infrastructure_ping_result")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class PingResult implements net.microfalx.heimdall.infrastructure.api.Ping {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "ping_id", nullable = false)
    @JoinColumn(name = "ping_id")
    @Name
    private Ping ping;

    @Column(name = "service_id", nullable = false)
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "server_id", nullable = false)
    @JoinColumn(name = "server_id")
    private Server server;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Description("The status of the ping")
    @Position(10)
    private Status status;

    @Column(name = "error_code")
    @Description("The status of the ping for an application service")
    @Position(20)
    private Integer errorCode;

    @Column(name = "error_message")
    @Description("The error message for the ping")
    @Position(21)
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    @Position(30)
    private ZonedDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    @Position(31)
    private ZonedDateTime endedAt;

    @Column(name = "duration", nullable = false)
    @Formattable()
    @Position(32)
    private Duration duration;

}
