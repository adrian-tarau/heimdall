package net.microfalx.heimdall.protocol.smtp;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A service which acts as an SMTP server.
 * <p>
 * The service listens for incoming emails, process them and stores the content in external storage and database.
 * <p>
 * The service uses <a href="https://github.com/davidmoten/subethasmtp">SubEtha SMTP</a> as an SMTP server.
 */
@Component
public class SmtpServer implements InitializingBean, BasicMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(SmtpServer.class);

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
            email.setSource(extractAddress(Arrays.asList(message.getFrom()).iterator().next()));
            Arrays.stream(message.getAllRecipients()).map(this::extractAddress).forEach(email::addTarget);
            email.setSentAt(message.getSentDate().toInstant().atZone(ZoneId.systemDefault()));
            email.setCreatedAt(email.getSentAt());
            email.setReceivedAt(ZonedDateTime.now());
            Body body = extractBody(email, message);
            if (body != null) email.setBody(body);
            extractParts(email, message).forEach(email::addPart);
            smtpService.handle(email);
        } catch (Exception e) {
            String message = "Failed to process email from '" + from + "' to '" + to + "'";
            logger.warn(message, e);
            throw new RejectException(message + ", root cause: " + e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    public void initialize() {
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
                .requireAuth(false)
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
        if (message.getContent() instanceof String) {
            Body body = new Body(email);
            body.setResource(MemoryResource.create((String) message.getContent()));
            return body;
        } else {
            return null;
        }
    }

    private Collection<Part> extractParts(Email email, MimeMessage message) throws MessagingException, IOException {
        if (!(message.getContent() instanceof Multipart multipart)) return Collections.emptyList();
        Collection<Part> parts = new ArrayList<>();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.getContent() instanceof String) {
                Body body = new Body(email);
                body.setResource(MemoryResource.create((String) message.getContent()));
                parts.add(body);
            } else if (isNotEmpty(bodyPart.getFileName())) {
                Attachment attachment = new Attachment(email);
                attachment.setResource(StreamResource.create(bodyPart.getInputStream()));
                parts.add(attachment);
            } else {
                Body body = new Body(email);
                body.setResource(StreamResource.create(bodyPart.getInputStream()));
                parts.add(body);
            }
        }
        return parts;
    }

}
