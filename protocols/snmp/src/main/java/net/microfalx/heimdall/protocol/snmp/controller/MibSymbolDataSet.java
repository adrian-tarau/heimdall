package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;
import net.microfalx.lang.annotation.Provider;

import java.util.Collection;

@Provider
public class MibSymbolDataSet extends MemoryDataSet<MibSymbol, PojoField<MibSymbol>, String> {

    public MibSymbolDataSet(DataSetFactory<MibSymbol, PojoField<MibSymbol>, String> factory, Metadata<MibSymbol, PojoField<MibSymbol>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Collection<MibSymbol> extractModels() {
        return getService(MibService.class).getSymbols();
    }
}
