package net.microfalx.heimdall.infrastructure.ping.overview;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.annotation.Renderable;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.chart.Function;
import net.microfalx.bootstrap.web.chart.Type;
import net.microfalx.bootstrap.web.chart.annotation.Chartable;
import net.microfalx.bootstrap.web.chart.annotations.Annotations;
import net.microfalx.bootstrap.web.chart.tooltip.Tooltip;
import net.microfalx.bootstrap.web.dataset.DataSetChartProvider;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.api.Status;
import net.microfalx.heimdall.infrastructure.core.overview.HealthAlertProvider;
import net.microfalx.heimdall.infrastructure.ping.PingService;
import net.microfalx.heimdall.infrastructure.ping.dataset.StatusAlertProvider;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.annotation.*;
import net.microfalx.metrics.Series;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.microfalx.bootstrap.web.dataset.DataSetChartProvider.PIE_HEIGHT;
import static net.microfalx.bootstrap.web.dataset.DataSetChartProvider.PIE_WIDTH;
import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.*;

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
    @Chartable(type = Type.PIE, width = PIE_WIDTH, height = PIE_HEIGHT, provider = HealthChartProvider.class)
    @OrderBy
    private Health health;

    @Position(26)
    @Description("The status of the last health check")
    @Formattable(alert = StatusAlertProvider.class)
    private Status status;

    @Position(29)
    @Description("The amount of time the last health check took")
    @Formattable(unit = Formattable.Unit.NANO_SECOND)
    @Label(value = "Last", group = "Durations")
    private Duration lastDuration;

    @Position(30)
    @Description("The minimum time it took to run the health check")
    @Formattable(unit = Formattable.Unit.NANO_SECOND)
    @Label(value = "Minimum", group = "Durations")
    private Duration minimumDuration;

    @Position(31)
    @Description("The average time it took to run the health check")
    @Formattable(unit = Formattable.Unit.NANO_SECOND)
    @Label(value = "Average", group = "Durations")
    private Duration averageDuration;

    @Position(32)
    @Description("The maximum time it took to run the health check")
    @Formattable(unit = Formattable.Unit.NANO_SECOND)
    @Label(value = "Maximum", group = "Durations")
    private Duration maximumDuration;

    @Position(33)
    @Description("The duration trend for the last health check executions")
    @Renderable(discard = true)
    @Label(value = "Trend", group = "Durations")
    @Chartable(width = TREND_CHART_WIDTH, height = TREND_CHART_HEIGHT, provider = DurationChartProvider.class)
    private Series durationTrend;

    @Asynchronous(false)
    public static class HealthChartProvider extends DataSetChartProvider<Ping, Field<Ping>, String> {

        @Override
        public void onUpdate(Chart chart) {
            PingService pingService = getBean(PingService.class);
            Ping model = getModel(chart);
            Map<Status, Long> statusCounts = pingService.getStatusCounts(model.getService(), model.getServer());
            List<String> labels = new ArrayList<>();
            List<Long> values = new ArrayList<>();
            statusCounts.forEach((k, v) -> {
                if (k != Status.NA) {
                    labels.add(EnumUtils.toName(k));
                    values.add(v);
                }
            });
            chart.setLabels(labels);
            chart.addSeries(net.microfalx.bootstrap.web.chart.series.Series.create(values));
        }
    }

    @Asynchronous(false)
    public static class DurationChartProvider extends DataSetChartProvider<Ping, Field<Ping>, String> {

        @Override
        public void onUpdate(Chart chart) {
            chart.setTooltip(Tooltip.durationNoTitleWithTimestamp()).setColors(Function.Color.negativeValue());
            chart.setAnnotations(Annotations.zeroY(TREND_ZERO_FILL_COLOR));
            PingService pingService = getBean(PingService.class);
            Ping model = getModel(chart);
            Series series = pingService.getSeries(model.getService(), model.getServer());
            chart.addSeries(series);
        }
    }


}
