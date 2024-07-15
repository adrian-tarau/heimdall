package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.dataset.DataSetChartProvider;
import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.annotation.Asynchronous;

import java.util.*;
import java.util.stream.Collectors;

@Asynchronous(false)
public class HealthPieChartProvider<E extends InfrastructureElement> extends DataSetChartProvider<E, Field<E>, String> {

    @Override
    public void onUpdate(Chart chart) {
        InfrastructureService infrastructureService = getBean(InfrastructureService.class);
        InfrastructureElement model = getModel(chart);
        Collection<Health> collection = infrastructureService.getHealthTrend(model);
        Map<Health, IntSummaryStatistics> stats = collection.stream().collect(Collectors.groupingBy(health -> health, Collectors.summarizingInt(value -> 1)));
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        stats.forEach((k, v) -> {
            labels.add(EnumUtils.toName(k));
            values.add(v.getCount());
        });
        chart.setLabels(labels);
        chart.addSeries(net.microfalx.bootstrap.web.chart.series.Series.create(values));
    }
}
