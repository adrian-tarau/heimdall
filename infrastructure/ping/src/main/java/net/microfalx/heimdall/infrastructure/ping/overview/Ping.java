package net.microfalx.heimdall.infrastructure.ping.overview;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.api.Status;
import net.microfalx.heimdall.infrastructure.core.overview.HealthAlertProvider;
import net.microfalx.lang.annotation.*;

import java.time.Duration;

@Name("Health Checks")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Ping extends NamedIdentityAware<String> {

    @Position(10)
    @Description("the Server that is ping")
    private Server server;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    @Position(11)
    @Width("150")
    private Service service;

    @Position(25)
    @Description("The health of the ping")
    @Formattable(alert = HealthAlertProvider.class)
    private Health health;

    @Position(26)
    @Description("The status of the last ping")
    @Formattable(alert = HealthAlertProvider.class)
    private Status status;

    @Position(29)
    @Description("The amount of time the last ping took")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Duration lastDuration;

    @Position(30)
    @Description("the minimum time it took to ping a service or server")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Duration minimumDuration;

    @Position(31)
    @Description("the average time it took to ping a service or server")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Duration averageDuration;

    @Position(32)
    @Description("the maximum time it took to ping a service or server")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Duration maximumDuration;


}
