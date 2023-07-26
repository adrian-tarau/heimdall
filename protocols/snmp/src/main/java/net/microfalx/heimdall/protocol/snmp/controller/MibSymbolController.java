package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.web.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.controller.DataSetController;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/snmp/symbol")
@DataSet(model = MibSymbol.class)
public class MibSymbolController extends DataSetController<MibSymbol, String> {
}
