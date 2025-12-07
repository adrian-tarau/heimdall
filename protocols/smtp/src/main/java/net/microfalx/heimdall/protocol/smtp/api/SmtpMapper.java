package net.microfalx.heimdall.protocol.smtp.api;

import net.microfalx.bootstrap.restapi.AbstractRestApiMapper;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Attachment;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.MimeType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class SmtpMapper extends AbstractRestApiMapper<SmtpEvent, SmtpDTO> {

    @Override
    protected SmtpDTO doToDto(SmtpEvent smtpEvent) {
        SmtpDTO smtpDTO = new SmtpDTO();
        smtpDTO.setId(smtpEvent.getId());
        smtpDTO.setSubject(smtpEvent.getSubject());
        smtpDTO.setFrom(Address.create(smtpEvent.getFrom().getType(), smtpEvent.getFrom().getValue(), smtpEvent.getFrom().getName()));
        smtpDTO.setTo(Address.create(smtpEvent.getTo().getType(), smtpEvent.getTo().getValue(), smtpEvent.getTo().getName()));
        smtpDTO.setSentAt(smtpEvent.getSentAt());
        smtpDTO.setReceivedAt(smtpEvent.getReceivedAt());
        smtpDTO.setMessage(Body.create(smtpEvent.getMessage().getResource()));
        Collection<Attachment> attachments = smtpEvent.getAttachments().stream().map(s -> Attachment.create(s.getPart().getResource())).toList();
        smtpDTO.setAttachments(attachments);
        smtpDTO.setAttachmentCount(smtpEvent.getAttachmentCount());
        return smtpDTO;
    }

    @Override
    protected SmtpEvent doToEntity(SmtpDTO smtpDTO) {
        SmtpEvent smtpEvent= new SmtpEvent();
        try {
            smtpEvent.setSubject(smtpDTO.getSubject());
            smtpEvent.setReceivedAt(smtpDTO.getReceivedAt());
            smtpEvent.setSentAt(smtpDTO.getSentAt());
            smtpEvent.setId(smtpDTO.getId());

            net.microfalx.heimdall.protocol.core.jpa.Address from= new net.microfalx.heimdall.protocol.core.jpa.Address();
            from.setCreatedAt(LocalDateTime.now());
            from.setType(smtpDTO.getFrom().getType());
            from.setName(smtpDTO.getFrom().getName());
            from.setValue(smtpDTO.getFrom().getValue());
            smtpEvent.setFrom(from);

            net.microfalx.heimdall.protocol.core.jpa.Address to= new net.microfalx.heimdall.protocol.core.jpa.Address();
            to.setCreatedAt(LocalDateTime.now());
            to.setType(smtpDTO.getTo().getType());
            to.setName(smtpDTO.getTo().getName());
            to.setValue(smtpDTO.getTo().getValue());
            smtpEvent.setTo(to);

            Part body= new Part();
            body.setId((int) smtpDTO.getId());
            body.setResource(smtpDTO.getMessage().loadAsString());
            body.setName(smtpDTO.getMessage().getName());
            body.setFileName(smtpDTO.getMessage().getFileName());
            body.setLength((int) smtpDTO.getMessage().getResource().length());
            body.setType(smtpDTO.getMessage().getType());
            body.setMimeType(MimeType.get(smtpDTO.getMessage().getMimeType()));
            body.setCreatedAt(LocalDateTime.now());
            smtpEvent.setMessage(body);

            smtpEvent.setAttachmentCount(smtpDTO.getAttachmentCount());
            for (Attachment attachment:smtpDTO.getAttachments()){
                SmtpAttachment jpaAttachment= new SmtpAttachment();
                jpaAttachment.setSmtpEvent(smtpEvent);
                Part part= new Part();
                part.setMimeType(MimeType.get(attachment.getMimeType()));
                part.setType(attachment.getType());
                part.setId((int) smtpDTO.getId());
                part.setName(attachment.getName());
                part.setFileName(attachment.getFileName());
                part.setCreatedAt(LocalDateTime.now());
                part.setResource(attachment.loadAsString());
                part.setLength((int) attachment.getResource().length());
                jpaAttachment.setPart(part);
                smtpEvent.addAttachment(jpaAttachment);
            }
        } catch (Exception e) {
            ExceptionUtils.rethrowException(e);
        }
        return smtpEvent;
    }
}
