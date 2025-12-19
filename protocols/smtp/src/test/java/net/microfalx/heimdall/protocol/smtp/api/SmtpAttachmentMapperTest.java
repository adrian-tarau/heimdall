package net.microfalx.heimdall.protocol.smtp.api;

import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Attachment;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MimeType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmtpAttachmentMapperTest {

    private final RestApiMapper<SmtpAttachment, SmtpAttachmentDTO> smtpAttachmentMapper = new SmtpAttachmentMapper();

    @Test
    void doToDto() throws IOException {
        SmtpAttachment smtpAttachment = new SmtpAttachment();
        smtpAttachment.setId(1L);
        Part part = createAttachment();
        smtpAttachment.setPart(part);
        smtpAttachment.setSmtpEvent(null);
        SmtpAttachmentDTO smtpAttachmentDTO = smtpAttachmentMapper.toDto(smtpAttachment);
        assertEquals(smtpAttachment.getId(), smtpAttachmentDTO.getId());
        assertEquals(smtpAttachment.getPart().getResource(), smtpAttachmentDTO.getAttachment().getResource().loadAsString());
        assertEquals(smtpAttachment.getPart().getLength(), smtpAttachmentDTO.getAttachment().getResource().length());
        assertEquals(smtpAttachment.getPart().getType(), smtpAttachmentDTO.getAttachment().getType());
        assertEquals(smtpAttachment.getPart().getMimeType(), MimeType.get(smtpAttachmentDTO.getAttachment().getMimeType()));
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

    private Part createAttachment() {
        Part part = new Part();
        part.setResource(URI.create(StringUtils.EMPTY_STRING).toString());
        part.setLength(part.getResource().length());
        part.setName("test attachment");
        part.setType(net.microfalx.heimdall.protocol.core.Part.Type.ATTACHMENT);
        part.setMimeType(MimeType.TEXT_PLAIN);
        part.setFileName("attachment.txt");
        part.setId(1);
        part.setCreatedAt(LocalDateTime.now());
        return part;
    }
}