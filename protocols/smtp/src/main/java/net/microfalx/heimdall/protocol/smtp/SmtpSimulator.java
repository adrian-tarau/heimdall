package net.microfalx.heimdall.protocol.smtp;

import net.datafaker.Faker;
import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SmtpSimulator extends ProtocolSimulator<SmtpEvent, SmtpClient> {

    private static AtomicLong COUNTER = new AtomicLong();
    private SmtpProperties smtpProperties;

    public SmtpSimulator(ProtocolSimulatorProperties properties, SmtpProperties smtpProperties) {
        super(properties);
        this.smtpProperties = smtpProperties;
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
        smtpClient.setPort(smtpProperties.getPort());
        smtpClient.setTransport(ProtocolClient.Transport.UDP);
        return Arrays.asList(smtpClient);
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
        SmtpEvent smtpEvent = new SmtpEvent();
        smtpEvent.setSource(sourceAddress);
        smtpEvent.addTarget(targetAddress);
        smtpEvent.setBody(createBody());
        smtpEvent.setName(getSubject(smtpEvent.getBody()));
        smtpEvent.setSentAt(ZonedDateTime.now());
        smtpEvent.setReceivedAt(ZonedDateTime.now());
        smtpEvent.setCreatedAt(ZonedDateTime.now());
        createAttachments(smtpEvent);
        client.send(smtpEvent);
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
            String fileName = random.nextFloat() > 0.5 ? faker.code().isbn13() : faker.commerce().productName();
            String extension = faker.file().extension();
            MimeType mimeType = extensionsToMimeType.getOrDefault(extension.toLowerCase(), MimeType.APPLICATION_OCTET_STREAM);
            Attachment attachment = createAttachment(mimeType);
            attachment.setFileName(fileName + "." + extension);
            attachment.setMimeType(mimeType);
            smtpEvent.addPart(attachment);
        }
    }

    private Attachment createAttachment(MimeType mimeType) {
        if (mimeType == MimeType.APPLICATION_OCTET_STREAM) {
            int maximumPartLength = getProperties().getMaximumPartLength();
            if ((COUNTER.get() % 100) == 0) {
                maximumPartLength = 5 * maximumPartLength;
            } else if ((COUNTER.get() % 500) == 0) {
                maximumPartLength = 100 * maximumPartLength;
            }
            return Attachment.create(getRandomBytes(getProperties().getMinimumPartLength(), maximumPartLength));
        } else {
            return Attachment.create(getRandomText());
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
