package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class SmtpTestHelper {

    private static final int START_PORT = 40000;
    private static final int PORT_RANGE = 10000;

    private static final AtomicInteger IDENTIFIER = new AtomicInteger(1);

    private ProtocolClient.Transport transport = ProtocolClient.Transport.UDP;
    private final SmtpProperties configuration;
    private Collection<Part> attachments = new ArrayList<>();

    SmtpTestHelper(SmtpProperties configuration) {
        this.configuration = configuration;
    }

    int getNextPort() {
        return START_PORT + ThreadLocalRandom.current().nextInt(PORT_RANGE);
    }

    void sendEmail(boolean html) throws IOException {
        SmtpClient client = new SmtpClient();
        client.setPort(configuration.getPort());
        SmtpEvent smtp = new SmtpEvent();
        smtp.setName("Test Email");
        smtp.setSource(Address.create(Address.Type.EMAIL, "john@company.com"));
        smtp.addTarget(Address.create(Address.Type.EMAIL, "doe@company.com"));
        client.setPort(configuration.getPort());
        if (html) {
            smtp.setBody(Body.html("<p>HTML Body</p>"));
        } else {
            smtp.setBody(Body.plain("Text Body"));
        }
        attachments.forEach(smtp::addPart);
        client.send(smtp);
    }

    public void addAttachment(String fileName, String text) throws IOException {
        addAttachment(fileName, MemoryResource.create(text));
    }

    public void addAttachment(String fileName, Resource resource) throws IOException {
        Attachment attachment = new Attachment();
        attachment.setFileName(fileName);
        attachment.setResource(resource);
        attachments.add(attachment);
    }

}
