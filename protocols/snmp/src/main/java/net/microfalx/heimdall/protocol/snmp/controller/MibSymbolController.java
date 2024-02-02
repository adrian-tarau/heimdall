package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/protocol/snmp/symbol")
@DataSet(model = MibSymbol.class)
@Help("protocol/snmp/symbol")
public class MibSymbolController extends DataSetController<MibSymbol, String> {
}
