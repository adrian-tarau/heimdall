package net.microfalx.heimdall.infrastructure.ping.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.bootstrap.metrics.Series;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.chart.Type;
import net.microfalx.bootstrap.web.chart.annotation.Chartable;
import net.microfalx.bootstrap.web.dataset.DataSetChartProvider;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.api.Status;
import net.microfalx.heimdall.infrastructure.core.overview.HealthAlertProvider;
import net.microfalx.heimdall.infrastructure.ping.PingService;
import net.microfalx.heimdall.infrastructure.ping.dataset.StatusAlertProvider;
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

    @Position(11)
    @Width("150")
    private Service service;

    @Position(25)
    @Description("The overall health of the health check")
    @Formattable(alert = HealthAlertProvider.class)
    @Chartable(type = Type.PIE, width = 20, height = 20, provider = HealthChartProvider.class)
    private Health health;

    @Position(26)
    @Description("The status of the last health check")
    @Formattable(alert = StatusAlertProvider.class)
    private Status status;

    @Position(29)
    @Description("The amount of time the last health check took")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Label(value = "Last", group = "Durations")
    private Duration lastDuration;

    @Position(30)
    @Description("The minimum time it took to run the health check")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Label(value = "Minimum", group = "Durations")
    private Duration minimumDuration;

    @Position(31)
    @Description("The average time it took to run the health check")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Label(value = "Average", group = "Durations")
    private Duration averageDuration;

    @Position(32)
    @Description("The maximum time it took to run the health check")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Label(value = "Maximum", group = "Durations")
    private Duration maximumDuration;

    @Position(33)
    @Description("The duration trend for the last health check executions")
    @Formattable(discard = true)
    @Label(value = "Trend", group = "Durations")
    @Chartable(width = 150, height = 20, provider = DurationChartProvider.class)
    private Series durationTrend;

    public static class HealthChartProvider extends DataSetChartProvider<Ping, Field<Ping>, String> {

        @Override
        public void onUpdate(Chart chart) {
            chart.addSeries(net.microfalx.bootstrap.web.chart.series.Series.create(3, 10, 7));
            chart.setLabels("L7/OK", "L4/CON", "L4/TOUT");
        }
    }

    public static class DurationChartProvider extends DataSetChartProvider<Ping, Field<Ping>, String> {

        @Override
        public void onUpdate(Chart chart) {
            PingService pingService = getBean(PingService.class);
            Ping model = getModel(chart);
            Series series = pingService.getSeries(model.getService(), model.getServer());
            chart.addSeries(series);
        }
    }


}
