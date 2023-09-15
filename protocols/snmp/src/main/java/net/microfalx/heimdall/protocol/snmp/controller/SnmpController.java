package net.microfalx.heimdall.protocol.snmp.controller;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEvent;
import net.microfalx.heimdall.protocol.snmp.jpa.SnmpEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/snmp")
@DataSet(model = SnmpEvent.class, viewTemplate = "snmp/event_view", viewClasses = "modal-xl")
public class SnmpController extends ProtocolController<SnmpEvent> {

    @Autowired
    private SnmpEventRepository snmpRepository;
}
