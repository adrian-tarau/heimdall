package net.microfalx.heimdall.protocol.smtp.api;

import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Attachment;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.resource.MimeType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmtpAttachmentMapperTest {

    private final RestApiMapper<SmtpAttachment, SmtpAttachmentDTO> smtpAttachmentMapper = new SmtpAttachmentMapper();

    @Test
    void doToDto() {

    }

    @Test
    void doToEntity() throws IOException {
        SmtpAttachmentDTO smtpAttachmentDTO = new SmtpAttachmentDTO();
        smtpAttachmentDTO.setId(1L);
        smtpAttachmentDTO.setAttachment(Attachment.create("test attachment"));
        SmtpDTO smtpDTO = new SmtpDTO();
        smtpDTO.setId(1L);
        smtpDTO.setAttachments(Collections.singletonList(Attachment.create("test attachment")));
        smtpDTO.setTo(Address.local());
        smtpDTO.setFrom(Address.local());
        smtpDTO.setSubject("test subject");
        smtpDTO.setMessage(Body.create("test message"));
        smtpDTO.setReceivedAt(LocalDateTime.now());
        smtpDTO.setSentAt(LocalDateTime.now());
        smtpAttachmentDTO.setSmtpDTO(smtpDTO);
        SmtpAttachment smtpAttachment = smtpAttachmentMapper.toEntity(smtpAttachmentDTO);
        assertEquals(smtpAttachmentDTO.getId(), smtpAttachment.getId());
        Part part = smtpAttachment.getPart();
        assertEquals(smtpAttachmentDTO.getAttachment().getResource().loadAsString(), part.getResource());
        assertEquals(smtpAttachmentDTO.getAttachment().getResource().length(), part.getLength());
        assertEquals(smtpAttachmentDTO.getAttachment().getName(), part.getName());
        assertEquals(smtpAttachmentDTO.getAttachment().getType(), part.getType());
        assertEquals(MimeType.get(smtpAttachmentDTO.getAttachment().getMimeType()), part.getMimeType());
        assertEquals(smtpAttachmentDTO.getAttachment().getFileName(), part.getFileName());
        assertEquals((int) smtpAttachmentDTO.getId(), part.getId());
        assertEquals(smtpAttachmentDTO.getSmtpDTO().getCreatedAt(), part.getCreatedAt());
        assertNotNull(smtpAttachment.getSmtpEvent());
    }
}