package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMib;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMibRepository;
import net.microfalx.heimdall.protocol.snmp.mib.MibModule;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.heimdall.protocol.snmp.mib.MibType;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.heimdall.protocol.snmp.controller.MibControllerUtilities.updateContext;

@Controller
@RequestMapping("/admin/protocol/snmp/mib")
@DataSet(model = SnmpMib.class, defaultQuery = "type = User",
        canAdd = false, canUpload = true, canDownload = true,
        viewTemplate = "snmp/mib_view", viewClasses = "modal-xl")
@Help("protocol/snmp/mib")
public class SnmpMibController extends DataSetController<SnmpMib, String> {

    @Autowired
    private SnmpMibRepository snmpMibRepository;

    @Autowired
    private MibService mibService;

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<SnmpMib, Field<SnmpMib>, String> dataSet, Model controllerModel, SnmpMib dataSetModel) {
        super.beforeView(dataSet, controllerModel, dataSetModel);
        MibModule module = mibService.findModule(dataSetModel.getModuleId());
        updateContext(controllerModel, module);
    }

    @Override
    protected void upload(net.microfalx.bootstrap.dataset.DataSet<SnmpMib, Field<SnmpMib>, String> dataSet, Model model, Resource resource) {
        MibModule mibModule = mibService.parseModule(resource);
        mibModule.setType(MibType.USER);
        mibService.updateModule(mibModule);
    }

    @Override
    protected Resource download(net.microfalx.bootstrap.dataset.DataSet<SnmpMib, Field<SnmpMib>, String> dataSet, Model controllerModel, SnmpMib dataSetModel) {
        return MemoryResource.create(dataSetModel.getContent(), dataSetModel.getFileName());
    }
}
