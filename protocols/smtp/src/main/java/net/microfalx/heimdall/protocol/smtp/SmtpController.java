package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import net.microfalx.resource.MimeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collection;

@Controller
@RequestMapping("/protocol/smtp")
@DataSet(model = SmtpEvent.class, viewTemplate = "smtp/event_view", viewClasses = "modal-xl")
public class SmtpController extends ProtocolController<SmtpEvent> {

    @Autowired
    private SmtpEventRepository smtpRepository;

    @Override
    protected void updateModel(net.microfalx.bootstrap.dataset.DataSet<SmtpEvent, Field<SmtpEvent>, Integer> dataSet, Model controllerModel, SmtpEvent dataSetModel, State state) {
        if (state == State.VIEW) {
            String bodyText = null;
            String bodyHtml = null;
            Collection<SmtpAttachment> realAttachments = new ArrayList<>();
            for (SmtpAttachment attachment : dataSetModel.getAttachments()) {
                net.microfalx.heimdall.protocol.core.jpa.Part part = attachment.getPart();
                if (part.getType() == Part.Type.ATTACHMENT) {
                    realAttachments.add(attachment);
                } else if (part.getType() == Part.Type.BODY) {
                    if (MimeType.TEXT_PLAIN.equals(part.getMimeType())) {
                        bodyText = part.getResource();
                    } else {
                        bodyHtml = part.getResource();
                    }
                }
            }
            controllerModel.addAttribute("attachments", realAttachments);
            controllerModel.addAttribute("bodyText", bodyText);
            controllerModel.addAttribute("bodyHtml", bodyHtml);
        }
    }
}
