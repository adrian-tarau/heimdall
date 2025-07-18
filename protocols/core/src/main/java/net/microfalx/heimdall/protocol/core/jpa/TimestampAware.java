package net.microfalx.heimdall.protocol.core.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.surrogate.IdentityAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Timestamp;
import net.microfalx.lang.annotation.Visible;

import java.time.LocalDateTime;

/**
 * A base class for all entities which can be timestamped.
 * <p>
 * All these entities are named entities too.
 */
@MappedSuperclass
@Getter
@Setter
@ToString(callSuper = true)
public abstract class TimestampAware extends IdentityAware<Long> {

    @Column(name = "created_at", nullable = false)
    @NotNull
    @Position(500)
    @Visible(modes = {Visible.Mode.VIEW})
    @Description("The time when the {name} event was created")
    private LocalDateTime createdAt;

    @Column(name = "sent_at", nullable = false)
    @NotNull
    @Position(501)
    @Visible(modes = {Visible.Mode.VIEW})
    @Description("The time when the {name} event was send")
    private LocalDateTime sentAt;

    @Column(name = "received_at", nullable = false)
    @Position(502)
    @OrderBy(OrderBy.Direction.DESC)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Timestamp
    @Description("The time when the {name} event was received")
    private LocalDateTime receivedAt;

}