package net.microfalx.heimdall.protocol.syslog;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEvent;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/syslog")
@DataSet(model = SyslogEvent.class, viewTemplate = "syslog/event_view", viewClasses = "modal-xl")
@Help("protocol/syslog")
public class SyslogController extends ProtocolController<SyslogEvent> {

    @Autowired
    private SyslogEventRepository syslogRepository;
}
