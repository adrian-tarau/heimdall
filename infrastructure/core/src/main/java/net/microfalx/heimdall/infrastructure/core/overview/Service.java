package net.microfalx.heimdall.infrastructure.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.lang.annotation.*;

@Name("Services")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Service extends NamedIdentityAware<String> {

    @Position(20)
    @Description("The type of the service")
    private net.microfalx.heimdall.infrastructure.api.Service.Type type = net.microfalx.heimdall.infrastructure.api.Service.Type.HTTP;

    @Position(21)
    @Description("The port where the service can be accessed")
    @Formattable(negativeValue = Formattable.NA, prettyPrint = false)
    private int port;

    @Position(22)
    @Description("The path to access the service (HTTP/HTTPs protocol)")
    private String path;

    @Position(25)
    @Description("The health of the service")
    @Formattable(alert = HealthAlertProvider.class)
    private Health health;

    @Position(30)
    @Label(value = "Total", group = "Servers")
    @Description("The number of services running on a server")
    private int serverCount;

    @Position(31)
    @Label(value = "Degraded", group = "Servers")
    @Description("The number of services in a degraded state on a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    private int degradedCount;

    @Position(32)
    @Label(value = "Unhealthy", group = "Servers")
    @Description("The number of services in an unhealthy state on a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    private int unhealthyCount;

    @Position(40)
    @Description("Indicates whether the service has any active instances")
    private boolean active;
}
