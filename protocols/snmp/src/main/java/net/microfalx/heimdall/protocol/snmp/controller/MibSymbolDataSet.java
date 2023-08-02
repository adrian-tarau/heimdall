package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.PojoDataSet;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;
import net.microfalx.lang.annotation.Provider;

@Provider
public class MibSymbolDataSet extends PojoDataSet<MibSymbol, PojoField<MibSymbol>, String> {

    public MibSymbolDataSet(DataSetFactory<MibSymbol, PojoField<MibSymbol>, String> factory, Metadata<MibSymbol, PojoField<MibSymbol>, String> metadata) {
        super(factory, metadata);
    }

}
