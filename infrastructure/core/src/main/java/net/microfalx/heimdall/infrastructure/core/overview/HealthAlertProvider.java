package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.infrastructure.api.Health;

public class HealthAlertProvider<M, F extends Field<M>> implements Formattable.AlertProvider<M, F, Health> {

    @Override
    public Alert provide(Health value, F field, M model) {
        Alert.Type type = switch (value) {
            case NA -> Alert.Type.LIGHT;
            case DEGRADED -> Alert.Type.WARNING;
            case UNAVAILABLE, UNHEALTHY -> Alert.Type.DANGER;
            case HEALTHY -> Alert.Type.SUCCESS;
        };
        return Alert.builder().type(type).minWidth(100).build();
    }
}
