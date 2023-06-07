package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachmentRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class SmtpService extends ProtocolService<Email> {

    @Autowired
    private SmtpConfiguration configuration;
    @Autowired
    private SmtpEventRepository repository;

    @Autowired
    private SmtpAttachmentRepository attachmentRepository;
    @Autowired
    private PartRepository partRepository;

    /**
     * Handle one SMTP message (email).
     * <p>
     * The method stores the email in the database, along with attachments. The email body and/or attachments are
     * stored in the shared data store and referenced as a {@link net.microfalx.resource.Resource}.
     *
     * @param email the STMP message
     */
    public void handle(Email email) {
        requireNonNull(email);
        handleDatabase(email);
        index(email);
    }

    /**
     * Stores the email into the database.
     *
     * @param email the email
     */
    private void handleDatabase(Email email) {
        SmtpEvent smtpEvent = new SmtpEvent();
        smtpEvent.setSentAt(email.getSentAt().toLocalDateTime());
        smtpEvent.setReceivedAt(email.getReceivedAt().toLocalDateTime());
        //smtp.setCreatedAt(email.getCreatedAt().toLocalDateTime());
        smtpEvent.setSubject(email.getName());
        updateAddresses(email, smtpEvent);
        List<SmtpAttachment> attachments = email.getParts().stream().map(part -> {
            net.microfalx.heimdall.protocol.core.jpa.Part jpaPart = new net.microfalx.heimdall.protocol.core.jpa.Part();
            jpaPart.setName(part.getName());
            jpaPart.setType(part.getType());
            jpaPart.setCreatedAt(part.getEvent().getCreatedAt().toLocalDateTime());
            jpaPart.setResource(part.getResource().toURI().toASCIIString());
            partRepository.save(jpaPart);
            SmtpAttachment attachment = new SmtpAttachment();
            attachment.setSmtp(smtpEvent);
            attachment.setPart(jpaPart);
            return attachment;
        }).toList();
        repository.save(smtpEvent);
        attachments.forEach(smtpEvent::addAttachment);
        attachmentRepository.saveAll(attachments);
    }

    private void updateAddresses(Email email, SmtpEvent smtpEvent) {
        smtpEvent.setFrom(lookupAddress(email.getSource()));
        smtpEvent.setTo(lookupAddress(email.getTargets().iterator().next()));
    }

    /**
     * Indexes an email.
     *
     * @param email the email
     */
    private void handleIndex(Email email) {

    }
}
