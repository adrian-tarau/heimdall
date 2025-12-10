package net.microfalx.heimdall.protocol.smtp.api;

import net.microfalx.bootstrap.restapi.AbstractRestApiMapper;
import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.heimdall.protocol.core.Attachment;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.resource.MimeType;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.time.LocalDateTime;

public class SmtpAttachmentMapper extends AbstractRestApiMapper<SmtpAttachment, SmtpAttachmentDTO> {

    private final RestApiMapper<SmtpEvent, SmtpDTO> smtpMapper = new SmtpMapper();

    @Override
    protected SmtpAttachmentDTO doToDto(SmtpAttachment smtpAttachment) {
        SmtpAttachmentDTO smtpAttachmentDTO = new SmtpAttachmentDTO();
        smtpAttachmentDTO.setId(smtpAttachment.getId());
        smtpAttachmentDTO.setSmtpDTO(smtpMapper.toDto(smtpAttachment.getSmtpEvent()));
        smtpAttachmentDTO.setAttachment(Attachment.create(smtpAttachment.getPart().getResource()));
        return smtpAttachmentDTO;
    }

    @Override
    protected SmtpAttachment doToEntity(SmtpAttachmentDTO smtpAttachmentDTO) {
        SmtpAttachment smtpAttachment = new SmtpAttachment();
        try {
            smtpAttachment.setId(smtpAttachmentDTO.getId());
            smtpAttachment.setSmtpEvent(smtpMapper.toEntity(smtpAttachmentDTO.getSmtpDTO()));
            smtpAttachment.setPart(createPart(smtpAttachmentDTO));
        } catch (Exception e) {
            ExceptionUtils.rethrowException(e);
        }
        return smtpAttachment;
    }

    private Part createPart(SmtpAttachmentDTO smtpAttachmentDTO) throws IOException {
        Part part = new Part();
        part.setResource(smtpAttachmentDTO.getAttachment().getResource().loadAsString());
        part.setName(smtpAttachmentDTO.getAttachment().getName());
        part.setType(smtpAttachmentDTO.getAttachment().getType());
        part.setMimeType(MimeType.get(smtpAttachmentDTO.getAttachment().getMimeType()));
        part.setFileName(smtpAttachmentDTO.getAttachment().getFileName());
        part.setLength((int) smtpAttachmentDTO.getAttachment().getResource().length());
        part.setId((int) smtpAttachmentDTO.getId());
        part.setCreatedAt(smtpAttachmentDTO.getSmtpDTO().getCreatedAt());
        return part;
    }
}
