package net.microfalx.heimdall.database.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.lang.annotation.*;
import net.microfalx.resource.Resource;
import org.hibernate.annotations.NaturalId;

import java.io.IOException;
import java.time.LocalDateTime;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

@Entity
@Table(name = "database_statements")
@Name("Statements")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Statement extends IdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "schema_id", nullable = false)
    @Position(2)
    @Label(value = "Name", group = "Database")
    @Description("The schema which owns the snapshot")
    private Schema schema;

    @Column(name = "database_type", length = 500)
    @Position(3)
    @Enumerated(EnumType.STRING)
    @Width(min = "50")
    @Label(value = "Type", group = "Database")
    @Description("The type of database")
    private net.microfalx.bootstrap.jdbc.support.Database.Type databaseType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Position(3)
    @Description("The user which executed the statement")
    private User user;

    @Column(name = "statement_id")
    @NaturalId
    @Position(1)
    @Description("The statement identifier")
    @Visible(false)
    private String statementId;

    @Column(name = "type")
    @Position(10)
    @Enumerated(EnumType.STRING)
    @Description("The statement type")
    private net.microfalx.bootstrap.jdbc.support.Statement.Type type;

    @Transient
    @Position(10)
    @Description("The SQL of the statement")
    @Name
    private String content;

    @Column(name = "length")
    @Position(11)
    @Description("The statement type")
    @Visible(value = false)
    private long length;

    @Column(name = "execution_count")
    @Position(20)
    @Label("Executions")
    @Description("The number of times this statement was executed (might be estimated by sampling sessions)")
    @Formattable(unit = Formattable.Unit.COUNT)
    private long executionCount;

    @Column(name = "total_duration")
    @Position(21)
    @Label(value = "Total", group = "Duration")
    @Description("The total duration of the execution time (might be estimated by sampling sessions)")
    @OrderBy(OrderBy.Direction.DESC)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private float totalDuration;

    @Column(name = "avg_duration")
    @Position(22)
    @Label(value = "Average", group = "Duration")
    @Description("The average duration of the execution time (might be estimated by sampling sessions)")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private float avgDuration;

    @Column(name = "min_duration")
    @Position(23)
    @Label(value = "Minimum", group = "Duration")
    @Description("The total duration of the execution time (might be estimated by sampling sessions)")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private float minDuration;

    @Column(name = "max_duration")
    @Position(24)
    @Label(value = "Maximum", group = "Duration")
    @Description("The total duration of the execution time (might be estimated by sampling sessions)")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private float maxDuration;

    @Column(name = "resource")
    @Position(100)
    @Visible(false)
    private String resource;

    @Column(name = "created_at")
    @Position(100)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was created")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    @Position(101)
    @Visible(modes = {Visible.Mode.BROWSE, Visible.Mode.VIEW})
    @Description("The timestamp when the {name} was updated last time (executed)")
    private LocalDateTime modifiedAt;

    public static String getStatementId(net.microfalx.bootstrap.jdbc.support.Statement statement, String userName) {
        requireNonNull(statement);
        requireNonNull(userName);
        return toIdentifier(userName + "_" + statement.getId());
    }

    public static Statement from(Schema schema, User user, net.microfalx.bootstrap.jdbc.support.Statement statement,
                                 Resource resource) throws IOException {
        Statement dbStatement = new Statement();
        dbStatement.setStatementId(getStatementId(statement, user.getName()));
        dbStatement.setType(statement.getType());
        dbStatement.setSchema(schema);
        dbStatement.setDatabaseType(schema.getType());
        dbStatement.setUser(user);
        dbStatement.setCreatedAt(LocalDateTime.now());
        dbStatement.setResource(resource.toURI().toASCIIString());
        dbStatement.setLength(resource.length());
        return dbStatement;
    }
}
