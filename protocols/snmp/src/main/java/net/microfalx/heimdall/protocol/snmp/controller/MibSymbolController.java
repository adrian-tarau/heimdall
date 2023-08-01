package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetController;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/snmp/symbol")
@DataSet(model = MibSymbol.class)
public class MibSymbolController extends DataSetController<MibSymbol, String> {
}
