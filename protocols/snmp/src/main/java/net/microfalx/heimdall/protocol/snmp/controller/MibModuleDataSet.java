package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.protocol.snmp.mib.MibModule;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.lang.annotation.Provider;

import java.util.List;

@Provider
public class MibModuleDataSet extends MemoryDataSet<MibModule, PojoField<MibModule>, String> {

    public MibModuleDataSet(DataSetFactory<MibModule, PojoField<MibModule>, String> factory, Metadata<MibModule, PojoField<MibModule>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected List<MibModule> extractModels() {
        return getService(MibService.class).getModules();
    }
}
