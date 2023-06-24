package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachmentRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class SmtpService extends ProtocolService<SmtpEvent> {

    @Autowired
    private SmtpConfiguration configuration;
    @Autowired
    private SmtpEventRepository repository;

    @Autowired
    private SmtpAttachmentRepository attachmentRepository;

    /**
     * Handle one SMTP message (email).
     * <p>
     * The method stores the email in the database, along with attachments. The email body and/or attachments are
     * stored in the shared data store and referenced as a {@link net.microfalx.resource.Resource}.
     *
     * @param smtpEvent the STMP message
     */
    public void accept(SmtpEvent smtpEvent) {
        requireNonNull(smtpEvent);
        handleDatabase(smtpEvent);
        index(smtpEvent);
    }

    /**
     * Stores the email into the database.
     *
     * @param smptEvent the email
     */
    private void handleDatabase(SmtpEvent smptEvent) {
        net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent smtpEvent = new net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent();
        smtpEvent.setCreatedAt(smptEvent.getCreatedAt().toLocalDateTime());
        smtpEvent.setSentAt(smptEvent.getSentAt().toLocalDateTime());
        smtpEvent.setReceivedAt(smptEvent.getReceivedAt().toLocalDateTime());
        smtpEvent.setSubject(smptEvent.getName());
        updateAddresses(smptEvent, smtpEvent);
        List<SmtpAttachment> attachments = smptEvent.getParts().stream().map(part -> {
            SmtpAttachment attachment = new SmtpAttachment();
            attachment.setSmtp(smtpEvent);
            attachment.setPart(persistPart(part));
            return attachment;
        }).toList();
        repository.save(smtpEvent);
        attachments.forEach(smtpEvent::addAttachment);
        attachmentRepository.saveAll(attachments);
    }

    private void updateAddresses(SmtpEvent smptEvent, net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent smtpEvent) {
        smtpEvent.setFrom(lookupAddress(smptEvent.getSource()));
        smtpEvent.setTo(lookupAddress(smptEvent.getTargets().iterator().next()));
    }

    /**
     * Indexes an email.
     *
     * @param smtpEvent the email
     */
    private void handleIndex(SmtpEvent smtpEvent) {

    }
}
