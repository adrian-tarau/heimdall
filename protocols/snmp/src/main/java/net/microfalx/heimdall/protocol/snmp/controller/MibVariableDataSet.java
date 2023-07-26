package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.web.dataset.AbstractDataSet;
import net.microfalx.bootstrap.web.dataset.DataSetFactory;
import net.microfalx.bootstrap.web.dataset.PojoDataSet;
import net.microfalx.bootstrap.web.dataset.PojoDataSetFactory;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class MibVariableDataSet extends PojoDataSet<MibVariable, PojoField<MibVariable>, String> {

    public MibVariableDataSet(DataSetFactory<MibVariable, PojoField<MibVariable>, String> factory, Metadata<MibVariable, PojoField<MibVariable>> metadata) {
        super(factory, metadata);
    }

    public static class Factory extends PojoDataSetFactory<MibVariable, PojoField<MibVariable>, String> {

        @Override
        protected AbstractDataSet<MibVariable, PojoField<MibVariable>, String> doCreate(Metadata<MibVariable, PojoField<MibVariable>> metadata) {
            return new MibVariableDataSet(this, metadata);
        }

        @Override
        public boolean supports(Metadata<MibVariable, PojoField<MibVariable>> metadata) {
            return isAssignable(metadata.getModel(), MibVariable.class);
        }
    }
}
