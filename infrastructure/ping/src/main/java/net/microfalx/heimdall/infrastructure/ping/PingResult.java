package net.microfalx.heimdall.infrastructure.ping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.core.i18n.I18n;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.DurationConverter;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.infrastructure.api.Status;
import net.microfalx.heimdall.infrastructure.core.system.Server;
import net.microfalx.heimdall.infrastructure.core.system.Service;
import net.microfalx.lang.annotation.*;

import java.time.Duration;
import java.time.ZonedDateTime;

@Entity
@ReadOnly
@Table(name = "infrastructure_ping_result")
@Getter
@Setter
@ToString(callSuper = true)
@Name("Health Check Results")
public class PingResult extends IdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "ping_id")
    @Name
    @Label("Name")
    @Position(1)
    private Ping ping;

    @ManyToOne
    @JoinColumn(name = "server_id")
    @Position(2)
    private Server server;

    @ManyToOne
    @JoinColumn(name = "service_id")
    @Position(3)
    private Service service;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Formattable(alert = AlertProvider.class)
    @Description("The status of the ping")
    @Position(10)
    private Status status;

    @Column(name = "started_at", nullable = false)
    @Position(30)
    @net.microfalx.bootstrap.dataset.annotation.OrderBy(OrderBy.Direction.DESC)
    @Timestamp
    private ZonedDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    @Position(31)
    private ZonedDateTime endedAt;

    @Column(name = "duration", nullable = false)
    @Convert(converter = DurationConverter.class)
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Position(32)
    @Width("90")
    private Duration duration;

    @Column(name = "error_code")
    @Description("The status of the ping for an application service")
    @Position(40)
    @Width("100")
    private Integer errorCode;

    @Column(name = "error_message")
    @Description("The error message for the ping")
    @Position(41)
    @Width("300")
    private String errorMessage;

    public static class AlertProvider extends ApplicationContextSupport implements Formattable.AlertProvider<PingResult, Field<PingResult>, Status> {

        @Override
        public Alert provide(Status value, Field<PingResult> field, PingResult model) {
            I18n i18n = getBean(I18n.class);
            String message = i18n.getText("heimdall.infrastructure.api.status." + value.name().toLowerCase(), false);
            Alert.Type type = Alert.Type.SUCCESS;
            if (value.isTimeout()) {
                type = Alert.Type.WARNING;
            } else if (value.isFailure()) {
                type = Alert.Type.DANGER;
            } else if (value == Status.NA) {
                type = Alert.Type.LIGHT;
            }
            return Alert.builder().type(type).message(message).build();
        }
    }

}
