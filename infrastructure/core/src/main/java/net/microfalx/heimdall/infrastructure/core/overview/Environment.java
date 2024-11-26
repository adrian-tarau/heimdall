package net.microfalx.heimdall.infrastructure.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.web.chart.Type;
import net.microfalx.bootstrap.web.chart.annotation.Chartable;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.lang.annotation.*;

import static net.microfalx.bootstrap.web.dataset.DataSetChartProvider.PIE_HEIGHT;
import static net.microfalx.bootstrap.web.dataset.DataSetChartProvider.PIE_WIDTH;
import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.TREND_CHART_HEIGHT;
import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.TREND_CHART_WIDTH;

@Name("Environments")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Environment extends InfrastructureElement {

    @Position(25)
    @Description("The health of the environment")
    @Formattable(alert = HealthAlertProvider.class)
    @Chartable(type = Type.PIE, width = PIE_WIDTH, height = PIE_HEIGHT, provider = HealthPieChartProvider.class)
    @OrderBy
    private Health health;

    @Position(30)
    @Label(value = "Total", group = "Servers")
    @Description("The number of servers supporting a cluster")
    private int totalCount;

    @Position(31)
    @Label(value = "Unavailable", group = "Servers")
    @Description("The number of servers which are not available (down) in a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int unavailableCount;

    @Position(32)
    @Label(value = "Unhealthy", group = "Servers")
    @Description("The number of servers in an unhealthy state in a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int unhealthyCount;

    @Position(33)
    @Label(value = "Degraded", group = "Servers")
    @Description("The number of servers in a degraded state in a cluster")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int degradedCount;

    @Position(100)
    @Description("The version of the environment")
    @Filterable
    private String version;
}
