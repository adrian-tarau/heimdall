package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/protocol/snmp/mo")
@DataSet(model = ManagedObject.class)
@Help("protocol/snmp/mo")
public class ManagedObjectController extends SystemDataSetController<ManagedObject, String> {

    public ManagedObjectController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
