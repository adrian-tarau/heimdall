package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.snmp.SnmpService;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEventRepository;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/protocol/snmp")
@DataSet(model = SnmpEvent.class, viewTemplate = "snmp/event_view", viewClasses = "modal-xl")
@Help("protocol/snmp")
public class SnmpController extends ProtocolController<SnmpEvent> {

    private final SnmpEventRepository snmpEventRepository;
    private final SnmpService snmpService;

    public SnmpController(DataSetService dataSetService, PartRepository partRepository, SnmpEventRepository snmpEventRepository, SnmpService snmpService) {
        super(dataSetService, partRepository);
        this.snmpEventRepository = snmpEventRepository;
        this.snmpService = snmpService;
    }

    @Override
    protected void beforeView(net.microfalx.bootstrap.dataset.DataSet<SnmpEvent, Field<SnmpEvent>, Integer> dataSet, Model controllerModel, SnmpEvent dataSetModel) {
        Resource messageResource = ResourceFactory.resolve(dataSetModel.getMessage().getResource());
        try {
            controllerModel.addAttribute(MESSAGE_ATTR, messageResource.loadAsString());
        } catch (IOException e) {
            controllerModel.addAttribute(MESSAGE_ATTR, "#ERROR: " + e.getMessage());
        }
        Attributes<?> attributes = snmpService.getAttributes(dataSetModel);
        controllerModel.addAttribute("attributes", attributes);

    }
}
