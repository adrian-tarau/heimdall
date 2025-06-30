package net.microfalx.heimdall.protocol.smtp.jpa;

import net.microfalx.bootstrap.dataset.AbstractDataSetExportCallback;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.smtp.SmtpServer;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;

@Provider
public class SmtpEventExportCallback extends AbstractDataSetExportCallback<SmtpEvent, Field<SmtpEvent>, Long> {

    @Override
    public boolean isExportable(DataSet<SmtpEvent, Field<SmtpEvent>, Long> dataSet, Field<SmtpEvent> field, boolean exportable) {
        return !(field.isId() || "attachments".equals(field.getName()));
    }

    @Override
    public Object getValue(DataSet<SmtpEvent, Field<SmtpEvent>, Long> dataSet, Field<SmtpEvent> field, SmtpEvent model, Object value) {
        if ("message".equals(field.getName())) {
            Resource resource = resolve(model.getMessage().getResource());
            SmtpServer server = getBean(SmtpServer.class);
            try {
                net.microfalx.heimdall.protocol.smtp.SmtpEvent event = server.createEvent(resource);
                return event != null ? event.getBody().loadAsString() : "No message body available";
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        } else {
            return super.getValue(dataSet, field, model, value);
        }
    }
}
