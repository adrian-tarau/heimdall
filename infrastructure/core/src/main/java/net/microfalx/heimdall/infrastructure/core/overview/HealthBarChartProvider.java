package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.metrics.Series;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.chart.annotations.Annotations;
import net.microfalx.bootstrap.web.chart.tooltip.Tooltip;
import net.microfalx.bootstrap.web.dataset.DataSetChartProvider;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.lang.annotation.Asynchronous;

import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.TREND_ZERO_FILL_COLOR;

@Asynchronous(false)
public class HealthBarChartProvider<E extends InfrastructureElement> extends DataSetChartProvider<E, Field<E>, String> {

    @Override
    public void onUpdate(Chart chart) {
        InfrastructureService infrastructureService = getBean(InfrastructureService.class);
        InfrastructureElement model = getModel(chart);
        Field<E> field = getField(chart);
        chart.setAnnotations(Annotations.zeroY(TREND_ZERO_FILL_COLOR));
        chart.setTooltip(Tooltip.valueWithTimestamp());
        Series trend = infrastructureService.getHealthTrend(model.getReference(), getHealthFromField(field));
        chart.addSeries(trend);
    }

    private Health getHealthFromField(Field<E> field) {
        String name = field.getName();
        return switch (name) {
            case "unavailableCount" -> Health.UNAVAILABLE;
            case "unhealthyCount" -> Health.UNHEALTHY;
            case "degradedCount" -> Health.DEGRADED;
            default -> Health.NA;
        };
    }
}
