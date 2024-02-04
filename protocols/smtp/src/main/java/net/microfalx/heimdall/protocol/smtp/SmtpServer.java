package net.microfalx.heimdall.protocol.smtp;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Attachment;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Part;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
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
import static net.microfalx.lang.IOUtils.getInputStreamAsBytes;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServer.class);

    @Autowired
    private SmtpProperties configuration;

    @Autowired
    private SmtpService smtpService;

    private Session session;

    @Override
    public void messageArrived(MessageContext context, String from, String to, byte[] data) throws RejectException {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Received email from {} for : {}", from, to);
        try {
            MimeMessage message = new MimeMessage(session, new ByteArrayInputStream(data));
            SmtpEvent smtpEvent = new SmtpEvent(message.getMessageID());
            smtpEvent.setName(message.getSubject());
            smtpEvent.setSource(extractAddress(Arrays.asList(message.getFrom()).iterator().next()));
            Arrays.stream(message.getAllRecipients()).map(this::extractAddress).forEach(smtpEvent::addTarget);
            smtpEvent.setSentAt(message.getSentDate().toInstant().atZone(ZoneId.systemDefault()));
            smtpEvent.setCreatedAt(smtpEvent.getSentAt());
            smtpEvent.setReceivedAt(ZonedDateTime.now());
            Body body = extractBody(message);
            if (body != null) smtpEvent.setBody(body);
            extractParts(message).forEach(smtpEvent::addPart);
            smtpService.accept(smtpEvent);
        } catch (Exception e) {
            String message = "Failed to process email from '" + from + "' to '" + to + "'";
            LOGGER.warn(message, e);
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
            return Address.email(((InternetAddress) mimeAddress).getPersonal(), ((InternetAddress) mimeAddress).getAddress());
        } else {
            return Address.email(mimeAddress.toString());
        }
    }

    private Body extractBody(MimeMessage message) throws MessagingException, IOException {
        if (message.getContent() instanceof String) {
            return (Body) Body.create((String) message.getContent()).setMimeType(MimeType.get(message.getContentType()));
        } else {
            return null;
        }
    }

    private Collection<Part> extractParts(MimeMessage message) throws MessagingException, IOException {
        if (!(message.getContent() instanceof Multipart multipart)) return Collections.emptyList();
        Collection<Part> parts = new ArrayList<>();
        extractParts(multipart, parts);
        return parts;
    }

    private void extractParts(Multipart multipart, Collection<Part> parts) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.getContent() instanceof Multipart) {
                extractParts((Multipart) bodyPart.getContent(), parts);
            } else if (isNotEmpty(bodyPart.getFileName())) {
                Attachment attachment = Attachment.create(createResource(bodyPart).withMimeType(MimeType.get(bodyPart.getContentType())));
                attachment.setFileName(bodyPart.getFileName());
                parts.add(attachment);
            } else {
                parts.add(Body.create(createResource(bodyPart).withMimeType(MimeType.get(bodyPart.getContentType()))));
            }
        }
    }

    private Resource createResource(BodyPart bodyPart) throws MessagingException, IOException {
        return MemoryResource.create(getInputStreamAsBytes(bodyPart.getInputStream())).withMimeType(MimeType.get(bodyPart.getContentType()));
    }

}
