package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.core.MimeType;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Iterator;

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
            Iterator<SmtpAttachment> bodies = dataSetModel.getAttachments().stream().filter(
                    a -> a.getPart().getType() == Part.Type.BODY).toList().iterator();
            String bodyText = null;
            String bodyHtml = null;
            while (bodies.hasNext()) {
                net.microfalx.heimdall.protocol.core.jpa.Part part = bodies.next().getPart();
                if (MimeType.TEXT_PLAIN.equals(part.getMimeType())) {
                    bodyText = part.getResource();
                } else {
                    bodyHtml = part.getResource();
                }
            }
            controllerModel.addAttribute("bodyText", bodyText);
            controllerModel.addAttribute("bodyHtml", bodyHtml);
        }
    }
}
