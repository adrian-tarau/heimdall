package net.microfalx.heimdall.protocol.smtp;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.helper.BasicMessageListener;
import org.subethamail.smtp.server.SMTPServer;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.IOUtils.getInputStreamAsBytes;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.TextUtils.isBinaryContent;

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

    private final SmtpProperties properties;
    private final SmtpGateway gateway;

    private final Metrics metrics = ProtocolUtils.getMetrics(Event.Type.SMTP).withGroup("Event");

    private final Collection<Consumer<SmtpEvent>> consumers = new ArrayList<>();

    public SmtpServer(SmtpProperties properties, SmtpGateway gateway) {
        requireNonNull(properties);
        requireNonNull(gateway);
        this.properties = properties;
        this.gateway = gateway;
    }

    public void addConsumer(Consumer<SmtpEvent> consumer) {
        requireNonNull(consumer);
        this.consumers.add(consumer);
    }

    @Override
    public void messageArrived(MessageContext context, String from, String to, byte[] data) throws RejectException {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Received email from {} for : {}", from, to);
        try {
            SmtpEvent event = createEvent(Resource.bytes(data));
            if (event != null) {
                fireEvent(event);
            } else {
                metrics.increment("Invalid");
            }
        } catch (Exception e) {
            String message = "Failed to process email from '" + from + "' to '" + to + "'";
            LOGGER.warn(message, e);
            throw new RejectException(message + ", root cause: " + e.getMessage());
        }
    }

    public SmtpEvent createEvent(Resource resource) throws MessagingException, IOException {
        requireNonNull(resource);
        MimeMessage message = new MimeMessage(gateway.getSession(), resource.getInputStream());
        if (isMimeMessageValid(message)) {
            return createEvent(message, resource);
        } else {
            metrics.increment("Invalid");
            return null;
        }
    }

    private SmtpEvent createEvent(MimeMessage message, Resource resource) throws MessagingException, IOException {
        String messageID = message.getMessageID();
        if (StringUtils.isEmpty(messageID)) messageID = UUID.randomUUID().toString();
        SmtpEvent smtpEvent = new SmtpEvent(messageID);
        smtpEvent.setName(message.getSubject());
        smtpEvent.setSource(extractAddress(Arrays.asList(message.getFrom()).iterator().next()));
        Arrays.stream(message.getAllRecipients()).map(this::extractAddress).forEach(smtpEvent::addTarget);
        smtpEvent.setSentAt(message.getSentDate().toInstant().atZone(ZoneId.systemDefault()));
        smtpEvent.setCreatedAt(smtpEvent.getSentAt());
        smtpEvent.setReceivedAt(ZonedDateTime.now());
        smtpEvent.setResource(resource);
        Body body = extractBody(message);
        if (body != null) smtpEvent.setBody(body);
        extractParts(message).forEach(smtpEvent::addPart);
        return smtpEvent;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    public void initialize() {
        initializeServer();
    }

    private void initializeServer() {
        SMTPServer server = SMTPServer
                .port(properties.getPort())
                .connectionTimeout(properties.getConnectionTimeout(), MILLISECONDS)
                .requireTLS(properties.isRequireTLS())
                .maxMessageSize(properties.getMaxMessageSize())
                .maxConnections(properties.getMaxConnections())
                .maxRecipients(properties.getMaxRecipients())
                .requireAuth(false)
                .messageHandler(this)
                .build();
        server.start();
        LOGGER.info("Listen on {},TLS {}", server.getPort(), server.getRequireTLS());
    }

    private boolean isMimeMessageValid(MimeMessage message) {
        boolean valid = false;
        try {
            valid = isAddressValid(message.getFrom()) && isAddressValid(message.getAllRecipients());
        } catch (Exception e) {
            // if it fails, it is also invalid
        }
        return valid;
    }

    private boolean isAddressValid(jakarta.mail.Address... addresses) {
        for (jakarta.mail.Address address : addresses) {
            if (!isAddressValid(address)) return false;
        }
        return true;
    }

    private boolean isAddressValid(jakarta.mail.Address address) {
        if (address == null) return false;
        if (address instanceof InternetAddress internetAddress) {
            return !(isBinaryContent(internetAddress.getAddress()) || isBinaryContent(internetAddress.getPersonal()));
        }
        return !isBinaryContent(address.toString());
    }

    private void fireEvent(SmtpEvent event) {
        if (consumers.isEmpty()) {
            LOGGER.warn("No consumer registered for SMTP event: {}", event.getName());
        } else {
            consumers.forEach(consumer -> {
                try {
                    consumer.accept(event);
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to process SMTP event: {}", event.getName(), e);
                }
            });
        }
    }

    private Address extractAddress(jakarta.mail.Address mimeAddress) {
        if (mimeAddress instanceof InternetAddress) {
            return Address.email(((InternetAddress) mimeAddress).getAddress(), ((InternetAddress) mimeAddress).getPersonal());
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
