package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMib;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpMibRepository;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/snmp/mib")
@DataSet(model = SnmpMib.class, canAdd = false, canUpload = true, canDownload = true)
public class SnmpMibController extends DataSetController<SnmpMib, String> {

    @Autowired
    private SnmpMibRepository snmpMibRepository;

    @Override
    protected void upload(net.microfalx.bootstrap.dataset.DataSet<SnmpMib, Field<SnmpMib>, String> dataSet, Model model, Resource resource) {
        System.out.println("Implement me");
    }

    @Override
    protected Resource download(net.microfalx.bootstrap.dataset.DataSet<SnmpMib, Field<SnmpMib>, String> dataSet, Model controllerModel, SnmpMib dataSetModel) {
        System.out.println("Implement me");
        return super.download(dataSet, controllerModel, dataSetModel);
    }
}
