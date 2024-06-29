package net.microfalx.heimdall.database.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.lang.annotation.*;
import net.microfalx.resource.Resource;

import java.time.LocalDateTime;

@Entity
@Table(name = "database_snapshots")
@Name("Snapshots")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Snapshot extends IdentityAware<Long> {

    @ManyToOne
    @JoinColumn(name = "schema_id", nullable = false)
    @Position(2)
    @Description("The schema which owns the snapshot")
    @Label(value = "Name", group = "Database")
    private Schema schema;

    @Column(name = "database_type", length = 500)
    @Position(3)
    @Enumerated(EnumType.STRING)
    @Width(min = "50")
    @Label(value = "Type", group = "Database")
    @Description("The type of database")
    private net.microfalx.bootstrap.jdbc.support.Database.Type databaseType;

    @Column(name = "session_active_count")
    @Position(10)
    @Label(value = "Active", group = "Session")
    @Description("The number of active sessions")
    private int sessionsActiveCount;

    @Column(name = "session_waiting_count")
    @Position(11)
    @Label(value = "Waiting", group = "Session")
    @Description("The number of sessions waiting for an event (but not blocked)")
    private int sessionsWaitingCount;

    @Column(name = "session_blocked_count")
    @Position(12)
    @Label(value = "Blocked", group = "Session")
    @Description("The number of sessions blocked waiting for a lock (or another database object)")
    private int sessionsBlockedCount;

    @Column(name = "session_inactive_count")
    @Position(13)
    @Label(value = "Inactive", group = "Session")
    @Description("The number of inactive sessions (no query is running)")
    private int sessionsInactiveCount;

    @Column(name = "session_killed_count")
    @Position(14)
    @Label(value = "Killed", group = "Session")
    @Description("The number of killed sessions waiting for resources to be released")
    @Visible(false)
    private int sessionsKilledCount;

    @Column(name = "transaction_running_count")
    @Position(20)
    @Label(value = "Running", group = "Transaction")
    @Description("The number of active transactions (the session might be active or idle)")
    private int transactionRunningCount;

    @Column(name = "transaction_blocked_count")
    @Position(21)
    @Label(value = "Blocked", group = "Transaction")
    @Description("The number of transactions blocked waiting for a lock (or another database object)")
    private int transactionBlockedCount;

    @Column(name = "transaction_committing_count")
    @Position(22)
    @Label(value = "Committing", group = "Transaction")
    @Description("The number of transactions in commit phase")
    private int transactionCommittingCount;

    @Column(name = "transaction_rolling_back_count")
    @Position(23)
    @Label(value = "Rolling Back", group = "Transaction")
    @Description("The number of transactions in roll back phase")
    private int transactionRollingBackCount;

    @Column(name = "incomplete")
    @Position(100)
    @Description("Indicates whether the snapshot is incomplete (not all metrics could be extracted)")
    private boolean incomplete;

    @Column(name = "created_at")
    @Position(110)
    @Description("The timestamp when the {name} was created")
    @OrderBy(OrderBy.Direction.DESC)
    @Timestamp
    private LocalDateTime createdAt;

    @Column(name = "resource")
    @Visible(false)
    private String resource;

    public static Snapshot from(Schema schema, net.microfalx.bootstrap.jdbc.support.Snapshot snapshot, Resource resource) {
        Snapshot ds = new Snapshot();
        ds.setSchema(schema);
        ds.setDatabaseType(schema.getType());
        ds.setSessionsActiveCount(snapshot.getSessionsActiveCount());
        ds.setSessionsBlockedCount(snapshot.getSessionsBlockedCount());
        ds.setSessionsWaitingCount(snapshot.getSessionsWaitingCount());
        ds.setSessionsInactiveCount(snapshot.getSessionsInactiveCount());
        ds.setSessionsKilledCount(snapshot.getSessionsKilledCount());
        ds.setTransactionRunningCount(snapshot.getTransactionRunningCount());
        ds.setTransactionBlockedCount(snapshot.getTransactionBlockedCount());
        ds.setTransactionCommittingCount(snapshot.getTransactionCommittingCount());
        ds.setTransactionRollingBackCount(snapshot.getTransactionRollingBackCount());
        ds.setIncomplete(snapshot.isIncomplete());
        ds.setResource(resource.toURI().toASCIIString());
        ds.setCreatedAt(LocalDateTime.now());
        return ds;
    }

}
