package net.microfalx.heimdall.infrastructure.core.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.metrics.Series;
import net.microfalx.bootstrap.metrics.Value;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.chart.Type;
import net.microfalx.bootstrap.web.chart.annotation.Chartable;
import net.microfalx.bootstrap.web.dataset.DataSetChartProvider;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.lang.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static net.microfalx.bootstrap.web.dataset.DataSetChartProvider.PIE_HEIGHT;
import static net.microfalx.bootstrap.web.dataset.DataSetChartProvider.PIE_WIDTH;
import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.TREND_CHART_HEIGHT;
import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.TREND_CHART_WIDTH;

@Name("Services")
@Getter
@Setter
@ToString(callSuper = true)
@ReadOnly
public class Service extends InfrastructureElement {

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
    @Chartable(type = Type.PIE, width = PIE_WIDTH, height = PIE_HEIGHT, provider = HealthPieChartProvider.class)
    @OrderBy
    private Health health;

    @Position(30)
    @Label(value = "Total", group = "Servers")
    @Description("The number of services running on a server")
    private int totalCount;

    @Position(31)
    @Label(value = "Unavailable", group = "Servers")
    @Description("The number of servers which are not available (down) holding a service")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int unavailableCount;

    @Position(32)
    @Label(value = "Unhealthy", group = "Servers")
    @Description("The number of services in an unhealthy state holding a service")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int unhealthyCount;

    @Position(33)
    @Label(value = "Degraded", group = "Servers")
    @Description("The number of servers in a degraded state holding a service")
    @Formattable(alert = HealthCountAlertProvider.class)
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = HealthBarChartProvider.class)
    private int degradedCount;

    @Position(40)
    @Description("Indicates whether the service has any active instances")
    private boolean active;

    public static class UnavailableChartProvider extends DataSetChartProvider<Service, Field<Service>, String> {

        @Override
        public void onUpdate(Chart chart) {
            long now = System.currentTimeMillis();
            Random random = ThreadLocalRandom.current();
            List<Value> values = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                values.add(Value.create(now + i, random.nextInt(10)));
            }
            chart.addSeries(Series.create("test", values));
        }
    }
}
