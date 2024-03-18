package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.protocol.snmp.mib.MibVariable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/protocol/snmp/variable")
@DataSet(model = MibVariable.class)
@Help("protocol/snmp/variable")
public class MibVariableController extends DataSetController<MibVariable, String> {
}
