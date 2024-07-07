package net.microfalx.heimdall.infrastructure.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.lang.annotation.*;

@Name("Clusters")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Cluster extends NamedIdentityAware<String> {

    @Position(10)
    @Description("The type of servers deployed in the cluster")
    private Server.Type type;

    @Position(20)
    @Description("The timezone where the cluster is deployed")
    @Lookup(model = TimeZoneLookup.class)
    private String timeZone;

    @Position(25)
    @Description("The health of the cluster")
    @Formattable(alert = HealthAlertProvider.class)
    private Health health;

    @Position(30)
    @Label(value = "Total", group = "Servers")
    @Description("The number of servers supporting a cluster")
    private int serverCount;

    @Position(31)
    @Label(value = "Degraded", group = "Servers")
    @Description("The number of servers in a degraded state in a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    private int degradedCount;

    @Position(32)
    @Label(value = "Unhealthy", group = "Servers")
    @Description("The number of servers in an unhealthy state in a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    private int unhealthyCount;

}
