package net.microfalx.heimdall.infrastructure.ping.dataset;

import net.microfalx.bootstrap.core.i18n.I18n;
import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.infrastructure.api.Status;

/**
 * A status provider to render the status of a ping.
 * @param <M> the model type
 */
public class StatusAlertProvider<M> extends ApplicationContextSupport implements Formattable.AlertProvider<M, Field<M>, Status> {

    @Override
    public Alert provide(Status value, Field<M> field, M model) {
        I18n i18n = getBean(I18n.class);
        String message = i18n.getText("heimdall.infrastructure.api.status." + value.name().toLowerCase(), false);
        Alert.Type type = Alert.Type.SUCCESS;
        if (value.isTimeout()) {
            type = Alert.Type.WARNING;
        } else if (value.isFailure()) {
            type = Alert.Type.DANGER;
        } else if (value == Status.NA) {
            type = Alert.Type.LIGHT;
        }
        return Alert.builder().type(type).minWidth(70).message(message).build();
    }
}
