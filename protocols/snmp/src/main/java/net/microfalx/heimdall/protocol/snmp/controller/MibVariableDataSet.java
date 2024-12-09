package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;
import net.microfalx.lang.annotation.Provider;

import java.util.Collection;

@Provider
public class MibVariableDataSet extends MemoryDataSet<MibVariable, PojoField<MibVariable>, String> {

    public MibVariableDataSet(DataSetFactory<MibVariable, PojoField<MibVariable>, String> factory, Metadata<MibVariable, PojoField<MibVariable>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Collection<MibVariable> extractModels(Filter filterable) {
        return getService(MibService.class).getVariables();
    }

}
