package jpa;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "infrastructure_ping_result")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class PingResult extends NamedTimestampAware {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "ping_id", nullable = false)
    @JoinColumn(name = "ping_id")
    @Visible(value = false)
    private Integer pingId;

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
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    @Position(31)
    private LocalDateTime endedAt;

    @Column(name = "duration", nullable = false)
    @Position(32)
    private Duration duration;

    public enum Status {
        SUCCESS,
        FAILURE,
        TIMEOUT
    }

}
