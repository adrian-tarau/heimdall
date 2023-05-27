package net.microfalx.heimdall.protocol.smtp;

import jakarta.annotation.PostConstruct;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.helper.BasicMessageListener;
import org.subethamail.smtp.server.SMTPServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A service which acts as an SMTP server.
 * <p>
 * The service listens for incoming emails, process them and stores the content in external storage and database.
 * <p>
 * The service uses <a href="https://github.com/davidmoten/subethasmtp">SubEtha SMTP</a> as an SMTP server.
 */
@Service
public class SmtpServerService implements BasicMessageListener {

    @Autowired
    private SmtpConfiguration configuration;

    @Autowired
    private SmtpService smtpService;

    private Session session;

    @Override
    public void messageArrived(MessageContext context, String from, String to, byte[] data) throws RejectException {
        try {
            MimeMessage message = new MimeMessage(session, new ByteArrayInputStream(data));
            Email email = new Email(message.getMessageID());
            email.setName(message.getSubject());
            email.setSource(extractAddress(message.getSender()));
            Arrays.stream(message.getAllRecipients()).map(this::extractAddress).forEach(email::addTarget);
            email.setSentAt(message.getSentDate().toInstant().atZone(ZoneId.systemDefault()));
            email.setCreatedAt(email.getSentAt());
            email.setReceivedAt(ZonedDateTime.now());
            extractParts(email, message).forEach(email::addPart);
            email.setBody(extractBody(email, message));
            smtpService.handle(email);
        } catch (Exception e) {
            throw new RejectException("Failed to process email from '" + from + "' to '" + to + ", root cause: " + e.getMessage());
        }
    }

    @PostConstruct
    protected void initialize() {
        initializeServer();
        initializeSession();
    }

    private void initializeServer() {
        SMTPServer server = SMTPServer
                .port(configuration.getPort())
                .connectionTimeout(configuration.getConnectionTimeout(), MILLISECONDS)
                .requireTLS(configuration.isRequireTLS())
                .maxMessageSize(configuration.getMaxMessageSize())
                .maxConnections(configuration.getMaxConnections())
                .maxRecipients(configuration.getMaxRecipients())
                .messageHandler(this)
                .build();
        server.start();
    }

    private void initializeSession() {
        Properties properties = new Properties();
        session = Session.getDefaultInstance(properties);
    }

    private Address extractAddress(jakarta.mail.Address mimeAddress) {
        if (mimeAddress instanceof InternetAddress) {
            return Address.create(((InternetAddress) mimeAddress).getPersonal(), ((InternetAddress) mimeAddress).getAddress());
        } else {
            return Address.create(mimeAddress.toString());
        }
    }

    private Body extractBody(Email email, MimeMessage message) throws MessagingException, IOException {
        Body body = new Body(email);
        if (message.getContent() instanceof String) {
            body.setResource(MemoryResource.create((String) message.getContent()));
        } else if (message.getContent() instanceof Multipart) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContent() instanceof String) {
                    body.setResource(MemoryResource.create((String) message.getContent()));
                    break;
                }
            }
        }
        return body;
    }

    private Collection<Part> extractParts(Email email, MimeMessage message) throws MessagingException, IOException {
        if (!(message.getContent() instanceof Multipart)) return Collections.emptyList();
        Multipart multipart = (Multipart) message.getContent();
        Collection<Part> parts = new ArrayList<>();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.getContent() instanceof String) {
                Body body = new Body(email);
                body.setResource(MemoryResource.create((String) message.getContent()));
                parts.add(body);
            } else {
                Attachment attachment = new Attachment(email);
                attachment.setResource(StreamResource.create(bodyPart.getInputStream()));
                parts.add(attachment);
            }
        }
        return parts;
    }

}
