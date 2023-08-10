package net.microfalx.heimdall.protocol.smtp;

import net.datafaker.Faker;
import net.microfalx.heimdall.protocol.core.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;

@Component
public class SmtpSimulator extends ProtocolSimulator<SmtpEvent, SmtpClient> {

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
        return Address.create(Address.Type.EMAIL, geRandomEmail());
    }

    /**
     * Invoked to create a list of source addresses.
     *
     * @return an address
     */
    @Override
    protected Address createTargetAddress() {
        return Address.create(Address.Type.EMAIL, geRandomEmail());
    }

    /**
     * Invoked to create the client.
     *
     * @return a non-null instance
     */
    @Override
    protected Collection<ProtocolClient<SmtpEvent>> createClients() {
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
    protected void simulate(ProtocolClient<SmtpEvent> client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        SmtpEvent smtpEvent = new SmtpEvent();
        smtpEvent.setSource(sourceAddress);
        smtpEvent.addTarget(targetAddress);
        smtpEvent.setBody(Body.create(getRandomText()));
        smtpEvent.setName(getSubject());
        smtpEvent.setSentAt(ZonedDateTime.now());
        smtpEvent.setReceivedAt(ZonedDateTime.now());
        smtpEvent.setCreatedAt(ZonedDateTime.now());
        createAttachments(smtpEvent);
        client.send(smtpEvent);
    }

    private void createAttachments(SmtpEvent smtpEvent) {
        Faker faker = new Faker();
        int attachmentCount = 0;
        if (random.nextFloat() > 0.8) {
            attachmentCount = 1 + random.nextInt(2);
        }
        for(int i = 0 ; i < attachmentCount; i++) {
            smtpEvent.addPart(Attachment.create(getRandomText()).setFileName(faker.file().fileName()));
        }
    }

    private String getSubject() {
        Faker faker = new Faker();
        return faker.book().title();
    }

    private String geRandomEmail() {
        Faker faker = new Faker();
        return faker.name().firstName() + "." + faker.name().lastName() + "@company.com";
    }
}
