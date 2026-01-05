package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import net.microfalx.heimdall.protocol.snmp.mib.MibSymbol;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/protocol/snmp/symbol")
@DataSet(model = MibSymbol.class)
@Help("protocol/snmp/symbol")
public class MibSymbolController extends SystemDataSetController<MibSymbol, String> {

    public MibSymbolController(DataSetService dataSetService) {
        super(dataSetService);
    }
}
