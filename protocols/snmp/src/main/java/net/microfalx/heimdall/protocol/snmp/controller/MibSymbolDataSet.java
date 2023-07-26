package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.web.dataset.AbstractDataSet;
import net.microfalx.bootstrap.web.dataset.DataSetFactory;
import net.microfalx.bootstrap.web.dataset.PojoDataSet;
import net.microfalx.bootstrap.web.dataset.PojoDataSetFactory;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class MibSymbolDataSet extends PojoDataSet<MibSymbol, PojoField<MibSymbol>, String> {

    public MibSymbolDataSet(DataSetFactory<MibSymbol, PojoField<MibSymbol>, String> factory, Metadata<MibSymbol, PojoField<MibSymbol>> metadata) {
        super(factory, metadata);
    }

    public static class Factory extends PojoDataSetFactory<MibSymbol, PojoField<MibSymbol>, String> {

        @Override
        protected AbstractDataSet<MibSymbol, PojoField<MibSymbol>, String> doCreate(Metadata<MibSymbol, PojoField<MibSymbol>> metadata) {
            return new MibSymbolDataSet(this, metadata);
        }

        @Override
        public boolean supports(Metadata<MibSymbol, PojoField<MibSymbol>> metadata) {
            return isAssignable(metadata.getModel(), MibSymbol.class);
        }
    }
}
