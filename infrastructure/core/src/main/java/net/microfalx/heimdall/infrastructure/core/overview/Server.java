package net.microfalx.heimdall.infrastructure.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.lang.annotation.*;

@Name("Servers")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Server extends NamedIdentityAware<String> {

    @Position(10)
    @Description("The type of server")
    private net.microfalx.heimdall.infrastructure.api.Server.Type type;

    @Position(20)
    @Description("The timezone where the server is deployed")
    @Lookup(model = TimeZoneLookup.class)
    private String timeZone;

    @Position(25)
    @Description("The health of the server")
    @Formattable(alert = HealthAlertProvider.class)
    private Health health;

    @Position(30)
    @Label(value = "Total", group = "Services")
    @Description("The number of services running on a server")
    private int serverCount;

    @Position(31)
    @Label(value = "Degraded", group = "Services")
    @Description("The number of services in a degraded state on a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    private int degradedCount;

    @Position(32)
    @Label(value = "Unhealthy", group = "Services")
    @Description("The number of services in an unhealthy state on a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    private int unhealthyCount;
}
