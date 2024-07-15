package net.microfalx.heimdall.infrastructure.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.web.chart.Type;
import net.microfalx.bootstrap.web.chart.annotation.Chartable;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.lang.annotation.*;

import static net.microfalx.bootstrap.web.dataset.DataSetChartProvider.PIE_HEIGHT;
import static net.microfalx.bootstrap.web.dataset.DataSetChartProvider.PIE_WIDTH;
import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.TREND_CHART_HEIGHT;
import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.TREND_CHART_WIDTH;

@Name("Servers")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Server extends InfrastructureElement {

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
    @Chartable(type = Type.PIE, width = PIE_WIDTH, height = PIE_HEIGHT, provider = HealthPieChartProvider.class)
    @OrderBy
    private Health health;

    @Position(30)
    @Label(value = "Total", group = "Services")
    @Description("The number of services running on a server")
    private int totalCount;

    @Position(31)
    @Label(value = "Unavailable", group = "Services")
    @Description("The number of services which are not available (down) on a server")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int unavailableCount;

    @Position(32)
    @Label(value = "Unhealthy", group = "Services")
    @Description("The number of services in an unhealthy state on a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int unhealthyCount;

    @Position(33)
    @Label(value = "Degraded", group = "Services")
    @Description("The number of services in a degraded state on a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int degradedCount;

}
