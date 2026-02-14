package net.microfalx.heimdall.protocol.smtp.simulator;

import net.datafaker.Faker;
import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolSimulator;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolSimulatorProperties;
import net.microfalx.heimdall.protocol.smtp.SmtpClient;
import net.microfalx.heimdall.protocol.smtp.SmtpEvent;
import net.microfalx.heimdall.protocol.smtp.SmtpProperties;
import net.microfalx.heimdall.protocol.smtp.SmtpServer;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;

@Component
public class SmtpSimulator extends ProtocolSimulator<SmtpEvent, SmtpClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpSimulator.class);

    private static final AtomicLong COUNTER = new AtomicLong();
    private final SmtpProperties properties;
    private final SmtpServer server;

    private final Metrics metrics = ProtocolUtils.getMetrics(Event.Type.SMTP).withGroup("Simulator");

    private volatile Iterator<MimeMessage> mimeMessages;

    public SmtpSimulator(ProtocolSimulatorProperties properties, SmtpProperties smtpProperties,
                         SmtpServer server) {
        super(properties);
        requireNonNull(smtpProperties);
        requireNonNull(server);
        this.properties = smtpProperties;
        this.server = server;
    }

    @Override
    protected Event.Type getEventType() {
        return Event.Type.SMTP;
    }

    /**
     * Invoked to create a list of target addresses.
     *
     * @return an address
     */
    @Override
    protected Address createSourceAddress() {
        Pair<String, String> pair = geRandomEmail();
        return Address.email(pair.getLeft(), pair.getRight());
    }

    /**
     * Invoked to create a list of source addresses.
     *
     * @return an address
     */
    @Override
    protected Address createTargetAddress() {
        return createSourceAddress();
    }

    /**
     * Invoked to create the client.
     *
     * @return a non-null instance
     */
    @Override
    protected Collection<SmtpClient> createClients() {
        SmtpClient smtpClient = new SmtpClient();
        smtpClient.setPort(properties.getPort());
        smtpClient.setTransport(ProtocolClient.Transport.UDP);
        return Arrays.asList(smtpClient);
    }

    @Override
    public boolean isEnabled() {
        if (super.isEnabled()) {
            return true;
        } else {
            return properties.isSimulatorEnabled();
        }
    }

    /**
     * Simulates an event.
     *
     * @param client        the client
     * @param sourceAddress the source address
     * @param targetAddress the target address
     * @param index         the event index
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void simulate(SmtpClient client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        SmtpEvent smtpEvent;
        if (shouldUseExternalDataSets()) {
            smtpEvent = doSimulateWithDataSet();
        } else {
            smtpEvent = doSimulateWithRandomData(sourceAddress, targetAddress, index);
        }
        if (smtpEvent != null) client.send(smtpEvent);
    }

    private SmtpEvent doSimulateWithDataSet() {
        if (mimeMessages == null || !mimeMessages.hasNext()) {
            ApacheMBoxDataSet dataSet = (ApacheMBoxDataSet) new ApacheMBoxDataSet.Factory().createDataSet();
            mimeMessages = dataSet.iterator();
        }
        if (mimeMessages.hasNext()) {
            Resource resource = Resource.text(mimeMessages.next().getContent());
            try {
                return server.createEvent(resource);
            } catch (Exception e) {
                metrics.increment("Invalid MimeMessage");
                LOGGER.debug("Failed to create MimeMessage from resource: {}, root cause {}", resource, getRootCauseDescription(e));
            }
        }
        return null;
    }

    private SmtpEvent doSimulateWithRandomData(Address sourceAddress, Address targetAddress, int index) {
        SmtpEvent smtpEvent = new SmtpEvent();
        smtpEvent.setSource(sourceAddress);
        smtpEvent.addTarget(targetAddress);
        smtpEvent.setBody(createBody());
        smtpEvent.setName(getSubject(smtpEvent.getBody()));
        smtpEvent.setSentAt(ZonedDateTime.now());
        smtpEvent.setReceivedAt(ZonedDateTime.now());
        smtpEvent.setCreatedAt(ZonedDateTime.now());
        createAttachments(smtpEvent);
        return smtpEvent;
    }

    private Body createBody() {
        if (random.nextFloat() > 0.5) {
            Resource resource = ClassPathResource.file("smtp/" + htmlFiles.get(random.nextInt(htmlFiles.size())));
            return (Body) Body.create(resource).setMimeType(MimeType.TEXT_HTML);
        } else {
            return (Body) Body.create(getRandomText()).setMimeType(MimeType.TEXT_PLAIN);
        }
    }

    private void createAttachments(SmtpEvent smtpEvent) {
        Faker faker = getFaker();
        int attachmentCount = 3;
        if (random.nextFloat() > 0.8) {
            attachmentCount = 1 + random.nextInt(2);
        }
        for (int i = 0; i < attachmentCount; i++) {
            boolean compresses = true;//random.nextFloat() > 0.5;
            String fileName = random.nextFloat() > 0.5 ? faker.code().isbn13() : faker.commerce().productName();
            String extension = faker.file().extension();
            if (compresses) extension += ".gz";
            MimeType mimeType = extensionsToMimeType.getOrDefault(extension.toLowerCase(), MimeType.APPLICATION_OCTET_STREAM);
            Attachment attachment = createAttachment(mimeType, compresses);
            attachment.setFileName(fileName + "." + extension);
            attachment.setMimeType(mimeType);
            smtpEvent.addPart(attachment);
        }
    }

    private Attachment createAttachment(MimeType mimeType, boolean compresses) {
        if (mimeType == MimeType.APPLICATION_OCTET_STREAM) {
            int maximumPartLength = getProperties().getMaximumPartLength();
            if ((COUNTER.get() % 100) == 0) {
                maximumPartLength = 5 * maximumPartLength;
            } else if ((COUNTER.get() % 500) == 0) {
                maximumPartLength = 100 * maximumPartLength;
            }
            return Attachment.create(getRandomBytes(getProperties().getMinimumPartLength(), maximumPartLength, compresses));
        } else {
            String randomText = getRandomText();
            if (compresses) {
                try {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    OutputStream outputStream = new GZIPOutputStream(buffer);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                    writer.write(randomText);
                    writer.close();
                    return Attachment.create(buffer.toByteArray());
                } catch (IOException e) {
                    return Attachment.create(randomText);
                }
            } else {
                return Attachment.create(randomText);
            }
        }
    }

    private String getSubject(Body body) {
        Faker faker = getFaker();
        String subject = faker.book().title();
        if (MimeType.TEXT_HTML.equals(body.getMimeType())) subject += " *";
        return subject;
    }

    private Pair<String, String> geRandomEmail() {
        Faker faker = getFaker();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        return Pair.of(firstName + "." + lastName + "@microfalx.net", firstName + " " + lastName);
    }

    private static final Map<String, MimeType> extensionsToMimeType = new HashMap<>();
    private static final List<String> htmlFiles = new ArrayList<>();

    static {
        extensionsToMimeType.put("txt", MimeType.TEXT_PLAIN);
        extensionsToMimeType.put("html", MimeType.TEXT_HTML);

        htmlFiles.add("form.html");
        htmlFiles.add("href.html");
        htmlFiles.add("lists_and_headings.html");
        htmlFiles.add("section.html");
        htmlFiles.add("table.html");
        htmlFiles.add("text_alignment.html");
    }
}
