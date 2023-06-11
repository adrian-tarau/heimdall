package net.microfalx.heimdall.protocol.core;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * A base class for all entities which can be timestamped.
 * <p>
 * All these entities are named entities too.
 */
@MappedSuperclass
public abstract class TimestampAware {

    @Column(name = "sent_at", nullable = false)
    @NotNull
    private LocalDateTime sentAt;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

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