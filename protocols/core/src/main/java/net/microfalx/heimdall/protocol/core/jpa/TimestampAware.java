package net.microfalx.heimdall.protocol.core.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.lang.annotation.Position;

import java.time.LocalDateTime;

/**
 * A base class for all entities which can be timestamped.
 * <p>
 * All these entities are named entities too.
 */
@MappedSuperclass
public abstract class TimestampAware {

    @Column(name = "created_at", nullable = false)
    @NotNull
    @Position(100)
    private LocalDateTime createdAt;

    @Column(name = "sent_at", nullable = false)
    @NotNull
    @Position(101)
    private LocalDateTime sentAt;

    @Column(name = "received_at", nullable = false)
    @Position(102)
    @OrderBy
    private LocalDateTime receivedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return "TimestampAware{" +
                "sentAt=" + sentAt +
                ", receivedAt=" + receivedAt +
                '}';
    }
}