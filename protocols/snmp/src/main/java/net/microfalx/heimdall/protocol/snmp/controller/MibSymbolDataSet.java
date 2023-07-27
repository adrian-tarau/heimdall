package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.web.dataset.DataSetFactory;
import net.microfalx.bootstrap.web.dataset.PojoDataSet;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;
import net.microfalx.lang.annotation.Provider;

@Provider
public class MibSymbolDataSet extends PojoDataSet<MibSymbol, PojoField<MibSymbol>, String> {

    public MibSymbolDataSet(DataSetFactory<MibSymbol, PojoField<MibSymbol>, String> factory, Metadata<MibSymbol, PojoField<MibSymbol>> metadata) {
        super(factory, metadata);
    }

}
