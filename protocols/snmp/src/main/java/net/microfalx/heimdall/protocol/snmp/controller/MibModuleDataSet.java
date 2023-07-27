package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.bootstrap.web.dataset.DataSetFactory;
import net.microfalx.bootstrap.web.dataset.PojoDataSet;
import net.microfalx.heimdall.protocol.snmp.mib.MibModule;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.lang.annotation.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Provider
public class MibModuleDataSet extends PojoDataSet<MibModule, PojoField<MibModule>, String> {

    public MibModuleDataSet(DataSetFactory<MibModule, PojoField<MibModule>, String> factory, Metadata<MibModule, PojoField<MibModule>> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Page<MibModule> doFindAll(Pageable pageable, Filter filterable) {
        List<MibModule> modules = getService(MibService.class).getModules();
        return new PageImpl<>(modules, pageable, modules.size());
    }

}
