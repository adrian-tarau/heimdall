package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.ProtocolService;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.smtp.jpa.Smtp;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachment;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class SmtpService extends ProtocolService<Email> {

    @Autowired
    private SmtpConfiguration configuration;

    @Autowired
    private SmtpRepository repository;

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
        Smtp smtp = new Smtp();
        smtp.setSentAt(email.getSentAt().toLocalDateTime());
        smtp.setReceivedAt(email.getReceivedAt().toLocalDateTime());
        //smtp.setCreatedAt(email.getCreatedAt().toLocalDateTime());
        smtp.setSubject(email.getName());
        updateAddresses(email, smtp);
        List<SmtpAttachment> attachments = email.getParts().stream().map(part -> {
            net.microfalx.heimdall.protocol.core.jpa.Part jpaPart = new net.microfalx.heimdall.protocol.core.jpa.Part();
            jpaPart.setName(part.getName());
            jpaPart.setCreatedAt(part.getEvent().getCreatedAt().toLocalDateTime());
            jpaPart.setResource(part.getResource().toURI().toASCIIString());
            SmtpAttachment attachment = new SmtpAttachment();
            attachment.setSmtp(smtp);
            attachment.setPart(jpaPart);
            return attachment;
        }).toList();
        attachments.forEach(smtp::addAttachment);
        repository.save(smtp);
    }

    private static void updateAddresses(Email email, Smtp smtp) {
        Address fromAddress = new Address();
        fromAddress.setType(Address.Type.EMAIL);
        fromAddress.setName(email.getSource().getName());
        fromAddress.setValue(email.getSource().getValue());
        smtp.setFrom(fromAddress);

        net.microfalx.heimdall.protocol.core.Address firstTarget = email.getTargets().iterator().next();
        Address toAddress = new Address();
        toAddress.setType(Address.Type.EMAIL);
        toAddress.setName(firstTarget.getName());
        toAddress.setValue(firstTarget.getValue());
        smtp.setTo(toAddress);
    }

    /**
     * Indexes an email.
     *
     * @param email the email
     */
    private void handleIndex(Email email) {

    }
}
