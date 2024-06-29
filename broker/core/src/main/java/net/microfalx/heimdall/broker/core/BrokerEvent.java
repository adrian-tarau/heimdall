package net.microfalx.heimdall.broker.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "broker_events")
@Name("Events")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class BrokerEvent extends IdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "cluster_id", nullable = false)
    @Position(2)
    @Label(value = "Name", group = "Broker")
    @Description("The broker which owns the topic")
    private Broker broker;

    @Column(name = "type")
    @Position(3)
    @Enumerated(EnumType.STRING)
    @Label(value = "Type", group = "Broker")
    @Width(min = "50")
    @Description("The type of broker")
    @NotNull
    private net.microfalx.bootstrap.broker.Broker.Type type;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    @Position(4)
    @Label(value = "Topic", group = "Broker")
    @Description("The topic used to extract events")
    private BrokerTopic topic;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    @Position(10)
    @Label(value = "Session")
    @Description("The session used to extract events")
    private BrokerSession session;

    @Column(name = "event_id")
    @Position(20)
    @Description("The event identifier")
    @NotNull
    @Filterable()
    private String eventId;

    @Column(name = "event_name")
    @Position(21)
    @Description("The name associated with the event (message/description)")
    @Name
    @NotNull
    @Filterable()
    private String eventName;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    @Position(500)
    @Description("The timestamp when the {name} was created")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "received_at")
    @NotNull
    @Position(501)
    @Description("The timestamp when the {name} was last time modified")
    @net.microfalx.bootstrap.dataset.annotation.OrderBy(OrderBy.Direction.DESC)
    @Timestamp
    private LocalDateTime receivedAt;
}
