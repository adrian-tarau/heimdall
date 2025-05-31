package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentLocator;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.heimdall.protocol.core.ProtocolController;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/protocol/smtp")
@DataSet(model = SmtpEvent.class, viewTemplate = "smtp/event_view", viewClasses = "modal-xl")
@Help("protocol/smtp")
public class SmtpController extends ProtocolController<SmtpEvent> {

    @Autowired
    private SmtpEventRepository smtpRepository;

    @Autowired
    private ContentService contentService;

    @Autowired
    private SmtpService smtpService;

    @Override
    protected void updateModel(net.microfalx.bootstrap.dataset.DataSet<SmtpEvent, Field<SmtpEvent>, Integer> dataSet, Model controllerModel, SmtpEvent dataSetModel, State state) {
        if (state == State.VIEW) {
            Content bodyText = null;
            Content bodyHtml = null;
            Collection<SmtpAttachment> realAttachments = new ArrayList<>();
            for (SmtpAttachment attachment : dataSetModel.getAttachments()) {
                net.microfalx.heimdall.protocol.core.jpa.Part part = attachment.getPart();
                if (part.getType() == Part.Type.ATTACHMENT) {
                    realAttachments.add(attachment);
                } else if (part.getType() == Part.Type.BODY) {
                    Content content = contentService.resolve(ContentLocator.create(Integer.toString(part.getId()), "smtp", part.getResource()));
                    if (MimeType.TEXT_PLAIN.equals(part.getMimeType())) {
                        bodyText = content;
                    } else {
                        bodyHtml = content;
                    }
                }
            }
            controllerModel.addAttribute("attachments", realAttachments);
            if (bodyHtml != null) {
                controllerModel.addAttribute("bodyHtml", bodyHtml);
            } else {
                controllerModel.addAttribute("bodyText", bodyText);
            }
        }
    }

    @PostMapping("{id}/forward")
    public void forward(@PathVariable("id") int id) {
        SmtpEvent event = smtpRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        net.microfalx.heimdall.protocol.core.jpa.Part message = event.getMessage();
        Resource resource = ResourceFactory.resolve(message.getResource()).withMimeType(message.getMimeType());
        smtpService.forward(resource);
    }
}
