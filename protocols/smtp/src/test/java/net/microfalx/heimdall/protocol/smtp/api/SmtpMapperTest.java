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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmtpMapperTest {

    private final RestApiMapper<SmtpEvent, SmtpDTO> smtpMapper = new SmtpMapper();

    @Test
    void toDto() throws IOException {
        SmtpEvent entity = new SmtpEvent();
        net.microfalx.heimdall.protocol.core.jpa.Address to = new net.microfalx.heimdall.protocol.core.jpa.Address();
        to.setType(Address.Type.EMAIL);
        to.setName("Recipient Name");
        to.setValue("bakim-idatu6@aol.com");
        entity.setTo(to);

        net.microfalx.heimdall.protocol.core.jpa.Address from = new net.microfalx.heimdall.protocol.core.jpa.Address();
        from.setType(Address.Type.EMAIL);
        from.setName("Sender Name");
        from.setValue("hagiyo_fuda13@aol.com");
        entity.setFrom(from);

        entity.setSentAt(LocalDateTime.now());
        entity.setReceivedAt(LocalDateTime.now());
        entity.setId(1L);
        entity.setSubject("Test Email");

        Part body = new Part();
        body.setResource("This is a test email");
        body.setLength("This is a test email".length());
        body.setType(net.microfalx.heimdall.protocol.core.Part.Type.BODY);
        body.setMimeType(MimeType.TEXT_PLAIN);
        body.setName("Test Email");
        entity.setMessage(body);

        List<SmtpAttachment> jpaAttachments = new ArrayList<>();
        Part attachmentPart1 = new Part();
        attachmentPart1.setResource("first attachment");
        attachmentPart1.setLength("first attachment".length());
        attachmentPart1.setType(net.microfalx.heimdall.protocol.core.Part.Type.ATTACHMENT);
        attachmentPart1.setMimeType(MimeType.TEXT_PLAIN);
        attachmentPart1.setName("attachment1.txt");
        attachmentPart1.setFileName("attachment1.txt");
        SmtpAttachment smtpAttachment1 = new SmtpAttachment();
        smtpAttachment1.setPart(attachmentPart1);
        jpaAttachments.add(smtpAttachment1);

        Part attachmentPart2 = new Part();
        attachmentPart2.setResource("second attachment");
        attachmentPart2.setLength("second attachment".length());
        attachmentPart2.setType(net.microfalx.heimdall.protocol.core.Part.Type.ATTACHMENT);
        attachmentPart2.setMimeType(MimeType.TEXT_PLAIN);
        attachmentPart2.setName("attachment2.txt");
        attachmentPart2.setFileName("attachment2.txt");
        SmtpAttachment smtpAttachment2 = new SmtpAttachment();
        smtpAttachment2.setPart(attachmentPart2);
        jpaAttachments.add(smtpAttachment2);

        entity.setAttachmentCount(jpaAttachments.size());

        SmtpDTO smtpDTO = smtpMapper.toDto(entity);

        assertEquals(entity.getId(), smtpDTO.getId());
        assertEquals(entity.getSubject(), smtpDTO.getSubject());
        assertEquals(entity.getReceivedAt(), smtpDTO.getReceivedAt());
        assertEquals(entity.getSentAt(), smtpDTO.getSentAt());

        assertEquals(entity.getTo().getType(), smtpDTO.getTo().getType());
        assertEquals(entity.getTo().getName(), smtpDTO.getTo().getName());
        assertEquals(entity.getTo().getValue(), smtpDTO.getTo().getValue());

        assertEquals(entity.getFrom().getType(), smtpDTO.getFrom().getType());
        assertEquals(entity.getFrom().getName(), smtpDTO.getFrom().getName());
        assertEquals(entity.getFrom().getValue(), smtpDTO.getFrom().getValue());

        assertEquals(entity.getMessage().getMimeType(), MimeType.get(smtpDTO.getMessage().getMimeType()));
        assertEquals(entity.getMessage().getFileName(), smtpDTO.getMessage().getFileName());
        assertEquals(entity.getMessage().getType(), smtpDTO.getMessage().getType());
        assertEquals(entity.getMessage().getResource(), smtpDTO.getMessage().getResource().loadAsString());
        assertEquals(entity.getMessage().getLength(), smtpDTO.getMessage().getResource().length());

        List<Attachment> attachments = new ArrayList<>(smtpDTO.getAttachments());
        for (int i = 0; i < attachments.size(); i++) {
            Part part = jpaAttachments.get(i).getPart();
            Attachment attachment = attachments.get(i);
            assertEquals(part.getName(), attachment.getName());
            assertEquals(part.getFileName(), attachment.getFileName());
            assertEquals(part.getResource(), attachment.getResource().loadAsString());
            assertEquals(part.getLength(), attachment.getResource().length());
            assertEquals(part.getType(), attachment.getType());
            assertEquals(part.getMimeType(), MimeType.get(attachment.getMimeType()));
        }
        assertEquals(entity.getAttachmentCount(), smtpDTO.getAttachmentCount());
    }

    @Test
    void toEntity() throws IOException {
        SmtpDTO smtpDTO = new SmtpDTO();
        smtpDTO.setTo(Address.local());
        smtpDTO.setFrom(Address.local());
        smtpDTO.setSentAt(LocalDateTime.now());
        smtpDTO.setReceivedAt(LocalDateTime.now());
        smtpDTO.setId(1L);
        smtpDTO.setSubject("Test Email");
        smtpDTO.setMessage(Body.create("This is a test email"));
        List<Attachment> attachments = List.of(Attachment.create("first attachment"), Attachment.create("second attachment"));
        smtpDTO.setAttachments(attachments);
        smtpDTO.setAttachmentCount(attachments.size());
        SmtpEvent entity = smtpMapper.toEntity(smtpDTO);

        assertEquals(smtpDTO.getId(), entity.getId());
        assertEquals(smtpDTO.getSubject(), entity.getSubject());
        assertEquals(smtpDTO.getReceivedAt(), entity.getReceivedAt());
        assertEquals(smtpDTO.getSentAt(), entity.getSentAt());

        assertEquals(smtpDTO.getTo().getType(), entity.getTo().getType());
        assertEquals(smtpDTO.getTo().getName(), entity.getTo().getName());
        assertEquals(smtpDTO.getTo().getValue(), entity.getTo().getValue());

        assertEquals(smtpDTO.getFrom().getType(), entity.getFrom().getType());
        assertEquals(smtpDTO.getFrom().getName(), entity.getFrom().getName());
        assertEquals(smtpDTO.getFrom().getValue(), entity.getFrom().getValue());

        assertEquals(MimeType.get(smtpDTO.getMessage().getMimeType()), entity.getMessage().getMimeType());
        assertEquals(smtpDTO.getMessage().getName(), entity.getMessage().getName());
        assertEquals(smtpDTO.getMessage().getFileName(), entity.getMessage().getFileName());
        assertEquals(smtpDTO.getMessage().getType(), entity.getMessage().getType());
        assertEquals(smtpDTO.getMessage().getResource().loadAsString(), entity.getMessage().getResource());
        assertEquals(smtpDTO.getMessage().getResource().length(), entity.getMessage().getLength());

        List<SmtpAttachment> jpaAttachments = new ArrayList<>(entity.getAttachments());
        for (int i = 0; i < attachments.size(); i++) {
            Part part = jpaAttachments.get(i).getPart();
            Attachment attachment = attachments.get(i);
            assertEquals(attachment.getName(), part.getName());
            assertEquals(attachment.getFileName(), part.getFileName());
            assertEquals(attachment.getResource().loadAsString(), part.getResource());
            assertEquals(attachment.getResource().length(), part.getLength());
            assertEquals(attachment.getType(), part.getType());
            assertEquals(MimeType.get(attachment.getMimeType()), part.getMimeType());
        }
        assertEquals(smtpDTO.getAttachmentCount(), entity.getAttachmentCount());
    }
}