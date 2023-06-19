package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.bootstrap.web.dataset.annotation.DataSet;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/smtp")
@DataSet(model = SmtpEvent.class)
public class SmtpController extends ProtocolController<SmtpEvent> {

    @Autowired
    private SmtpEventRepository smtpRepository;
}
