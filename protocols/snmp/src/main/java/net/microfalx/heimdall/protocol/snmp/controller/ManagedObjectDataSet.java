package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.dataset.MemoryDataSet;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.PojoField;
import net.microfalx.heimdall.protocol.snmp.AgentServer;
import net.microfalx.heimdall.protocol.snmp.SnmpUtils;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Provider;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.agent.mo.MOTableIndex;
import org.snmp4j.smi.OID;

import java.util.ArrayList;
import java.util.Collection;

import static net.microfalx.heimdall.protocol.snmp.SnmpUtils.*;

@Provider
public class ManagedObjectDataSet extends MemoryDataSet<ManagedObject, PojoField<ManagedObject>, String> {

    private MibService mibService;

    public ManagedObjectDataSet(DataSetFactory<ManagedObject, PojoField<ManagedObject>, String> factory, Metadata<ManagedObject, PojoField<ManagedObject>, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected Iterable<ManagedObject> extractModels(Filter filterable) {
        Collection<ManagedObject> managedObjects = new ArrayList<>();
        getService(AgentServer.class).getManagedObjects()
                .forEachRemaining(e -> managedObjects.add(from(e.getValue())));
        return managedObjects;
    }

    private ManagedObject from(org.snmp4j.agent.ManagedObject<?> mo) {
        ManagedObject amo = new ManagedObject();
        amo.setId(getScopeID(mo.getScope()));
        amo.setName(getMoName(mo));
        amo.setType(describeMoType(mo));
        amo.setValue(describeMoValue(mo, false));
        updateTable(amo, mo);
        return amo;
    }

    private void updateTable(ManagedObject amo, org.snmp4j.agent.ManagedObject<?> mo) {
        if (!(mo instanceof MOTable<?, ?, ?> table)) return;
        MOTableIndex indexDef = table.getIndexDef();
        amo.setColumnCount(table.getColumnCount());
        amo.setRowCount(indexDef.size());
    }

    private String getMoName(org.snmp4j.agent.ManagedObject<?> mo) {
        OID oid = SnmpUtils.getScopeOid(mo.getScope());
        if (oid != null) {
            String name = getMibService().findName(oid.toDottedString());
            if (StringUtils.isNotEmpty(name)) return name;
        }
        return SnmpUtils.describeScope(mo.getScope());
    }

    private MibService getMibService() {
        if (mibService == null) mibService = getService(MibService.class);
        return mibService;
    }


}


