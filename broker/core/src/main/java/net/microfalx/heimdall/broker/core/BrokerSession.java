package net.microfalx.heimdall.broker.core;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.jpa.DurationConverter;
import net.microfalx.lang.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "broker_sessions")
@Name("Sessions")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
@ReadOnly
public class BrokerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @Name
    @Visible(true)
    @Position(1)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cluster_id", nullable = false)
    @Position(2)
    @Label(value = "Name", group = "Broker")
    @Description("The broker which owns the topic")
    private Broker broker;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    @Position(2)
    @Label(value = "Topic", group = "Broker")
    @Description("The topic used to extract events")
    private BrokerTopic topic;

    @Column(name = "type")
    @Position(3)
    @Enumerated(EnumType.STRING)
    @Label(value = "Type", group = "Broker")
    @Width(min = "50")
    @Description("The type of broker")
    @NotNull
    private net.microfalx.bootstrap.broker.Broker.Type type;

    @Column(name = "status")
    @Position(10)
    @Enumerated(EnumType.STRING)
    @Width(min = "50")
    @Description("The status of the session")
    @NotNull
    private Status status;

    @Column(name = "started_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was created")
    @net.microfalx.bootstrap.dataset.annotation.OrderBy(OrderBy.Direction.DESC)
    @Timestamp
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "ended_at")
    @Position(501)
    @Description("The timestamp when the {name} was last time modified")
    private LocalDateTime endedAt;

    @Column(name = "duration")
    @Position(502)
    @Convert(converter = DurationConverter.class)
    private Duration duration;

    @Column(name = "total_event_count")
    @Label(value = "Total", group = "Event Count")
    @Position(600)
    @Formattable(unit = Formattable.Unit.COUNT)
    private int totalEventCount;

    @Column(name = "sampled_event_count")
    @Label(value = "Sampled", group = "Event Count")
    @Position(601)
    @Formattable(unit = Formattable.Unit.COUNT)
    private int sampledEventCount;

    @Column(name = "total_event_size")
    @Label(value = "Total", group = "Event Size")
    @Position(602)
    @Formattable(unit = Formattable.Unit.BYTES)
    private long totalEventSize;

    @Column(name = "sampled_event_size")
    @Label(value = "Sampled", group = "Event Size")
    @Position(603)
    @Formattable(unit = Formattable.Unit.BYTES)
    private long sampledEventSize;

    @Column(name = "failure_message")
    @Position(700)
    private String failureMessage;

    @Column(name = "resource")
    @Position(602)
    @Visible(value = false)
    private String resource;

    /**
     * An enum for the session status
     */
    public enum Status {
        SUCCESSFUL,
        FAILED
    }
}
