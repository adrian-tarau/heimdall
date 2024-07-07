package net.microfalx.heimdall.infrastructure.core.overview;

import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.model.Field;

public class HealthCountAlertProvider<M, F extends Field<M>> implements Formattable.AlertProvider<M, F, Integer> {

    @Override
    public Alert provide(Integer value, F field, M model) {
        Alert.Type type = value > 0 ? Alert.Type.WARNING : Alert.Type.SUCCESS;
        return Alert.builder().type(type).build();
    }
}
