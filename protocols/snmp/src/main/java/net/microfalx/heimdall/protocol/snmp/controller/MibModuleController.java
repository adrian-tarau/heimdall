package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.protocol.snmp.mib.MibModule;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.heimdall.protocol.snmp.controller.MibControllerUtilities.updateContext;

@Controller
@RequestMapping("/system/protocol/snmp/module")
@DataSet(model = MibModule.class,viewTemplate = "snmp/module_view", viewClasses = "modal-xl")
@Help("protocol/snmp/module")
public class MibModuleController extends SystemDataSetController<MibModule, String> {

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<MibModule, Field<MibModule>, String> dataSet, Model controllerModel, MibModule module) {
        super.beforeView(dataSet, controllerModel, module);
        updateContext(controllerModel, module);
    }
}
