package net.microfalx.heimdall.database.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "database_statements")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class StatementStatistics {

    @Id
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "execution_count")
    private long executionCount;

    @Column(name = "total_duration")
    private float totalDuration;

    @Column(name = "avg_duration")
    private float avgDuration;

    @Column(name = "min_duration")
    private float minDuration;

    @Column(name = "max_duration")
    private float maxDuration;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
