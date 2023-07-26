package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.web.dataset.AbstractDataSet;
import net.microfalx.bootstrap.web.dataset.DataSetFactory;
import net.microfalx.bootstrap.web.dataset.PojoDataSet;
import net.microfalx.bootstrap.web.dataset.PojoDataSetFactory;
import net.microfalx.heimdall.protocol.snmp.mib.MibModule;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;

import java.util.List;

import static org.apache.commons.lang3.ClassUtils.isAssignable;

public class MibModuleDataSet extends PojoDataSet<MibModule, PojoField<MibModule>, String> {

    public MibModuleDataSet(DataSetFactory<MibModule, PojoField<MibModule>, String> factory, Metadata<MibModule, PojoField<MibModule>> metadata) {
        super(factory, metadata);
    }

    @Override
    protected List<MibModule> doFindAll() {
        return getService(MibService.class).getModules();
    }

    public static class Factory extends PojoDataSetFactory<MibModule, PojoField<MibModule>, String> {

        @Override
        protected AbstractDataSet<MibModule, PojoField<MibModule>, String> doCreate(Metadata<MibModule, PojoField<MibModule>> metadata) {
            return new MibModuleDataSet(this, metadata);
        }

        @Override
        public boolean supports(Metadata<MibModule, PojoField<MibModule>> metadata) {
            return isAssignable(metadata.getModel(), MibModule.class);
        }
    }
}
