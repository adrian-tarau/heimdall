package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;
import net.microfalx.lang.annotation.Provider;

@Provider
public class MibVariableDataSet extends PojoDataSet<MibVariable, PojoField<MibVariable>, String> {

    public MibVariableDataSet(DataSetFactory<MibVariable, PojoField<MibVariable>, String> factory, Metadata<MibVariable, PojoField<MibVariable>, String> metadata) {
        super(factory, metadata);
    }

}