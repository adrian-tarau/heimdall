package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/protocol/smtp")
@DataSet(model = SmtpEvent.class, viewTemplate = "smtp_view", viewClasses = "modal-xl")
public class SmtpController extends ProtocolController<SmtpEvent> {

    @Autowired
    private SmtpEventRepository smtpRepository;

    @Override
    protected void updateModel(net.microfalx.bootstrap.dataset.DataSet<SmtpEvent, Field<SmtpEvent>, Integer> dataSet, Model controllerModel, SmtpEvent dataSetModel, State state) {
        if (state == State.VIEW) {
            controllerModel.addAttribute("attachments", dataSetModel.getAttachments().stream().filter(
                    a -> a.getPart().getType() == Part.Type.ATTACHMENT).toList());
        }
    }
}
