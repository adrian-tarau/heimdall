package net.microfalx.heimdall.protocol.gelf.jpa;

import net.microfalx.bootstrap.dataset.AbstractDataSetExportCallback;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.lang.annotation.Provider;

@Provider
public class GelfEventDataSetExportCallback extends AbstractDataSetExportCallback<GelfEvent, Field<GelfEvent>, Long> {

    @Override
    public String getFieldName(DataSet<GelfEvent, Field<GelfEvent>, Long> dataSet, Field<GelfEvent> field) {
        if ("longMessage".equalsIgnoreCase(field.getName())) {
            return "message";
        } else {
            return super.getFieldName(dataSet, field);
        }
    }

    @Override
    public String getLabel(DataSet<GelfEvent, Field<GelfEvent>, Long> dataSet, Field<GelfEvent> field) {
        if ("longMessage".equalsIgnoreCase(field.getName())) {
            return "Message";
        } else {
            return super.getLabel(dataSet, field);
        }
    }

    @Override
    public boolean isExportable(DataSet<GelfEvent, Field<GelfEvent>, Long> dataSet, Field<GelfEvent> field, boolean exportable) {
        return !"shortMessage".equalsIgnoreCase(field.getName());
    }

    @Override
    public Object getValue(DataSet<GelfEvent, Field<GelfEvent>, Long> dataSet, Field<GelfEvent> field, GelfEvent model, Object value) {
        if ("longMessage".equalsIgnoreCase(field.getName())) {
            Part part = model.getLongMessage();
            if (part == null) part = model.getShortMessage();
            return load(part.getResource());
        } else {
            return super.getValue(dataSet, field, model, value);
        }
    }
}
