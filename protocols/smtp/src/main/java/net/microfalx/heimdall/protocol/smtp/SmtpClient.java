package net.microfalx.heimdall.protocol.smtp;

import jakarta.mail.MessagingException;
import jakarta.mail.util.ByteArrayDataSource;
import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.resource.MimeType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class SmtpClient extends ProtocolClient<SmtpEvent> {

    private JavaMailSender mailSender;

    public SmtpClient() {
        doSetTransport(Transport.TCP, false);
    }

    /**
     * Returns the default port.
     * <p>
     * Each transport protocol has a different port.
     *
     * @return the port
     */
    @Override
    protected int getDefaultPort() {
        return 25;
    }

    /**
     * Subclasses will implement this method to send an event.
     *
     * @param event the event
     */
    @Override
    protected void doSend(SmtpEvent event) {
        if (event.getBody() == null) throw new ProtocolException("A body is required for an SMTP event");
        initializeMailSender();
        mailSender.send(mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultiPart(event), "UTF-8");
            message.setFrom(event.getSource().getValue(), event.getSource().getName());
            for (Address target : event.getTargets()) {
                message.addTo(target.getValue(), target.getName());
            }
            message.setSubject(event.getName());
            message.setSentDate(Date.from(Instant.now()));
            handleBody(event, message);
            handleAttachments(event, message);
        });
    }

    @Override
    protected void configurationChanged() {
        super.configurationChanged();
        initializeMailSender();
    }

    private void handleBody(SmtpEvent event, MimeMessageHelper message) throws MessagingException {
        Body textBody = null;
        Body htmlBody = null;
        for (Part part : event.getParts()) {
            if (part instanceof Body) {
                if (MimeType.TEXT_PLAIN.equals(part.getMimeType())) {
                    textBody = (Body) part;
                } else if (MimeType.TEXT_HTML.equals(part.getMimeType())) {
                    htmlBody = (Body) part;
                }
            }
        }
        if (textBody != null && htmlBody != null) {
            message.setText(textBody.loadAsString(), textBody.loadAsString());
        } else if (htmlBody != null) {
            message.setText(htmlBody.loadAsString(), true);
        } else {
            message.setText(textBody.loadAsString(), false);
        }
    }

    private void handleAttachments(SmtpEvent event, MimeMessageHelper message) {
        if (!event.hasAttachments()) return;
        for (Part part : event.getParts()) {
            if (part instanceof Attachment) {
                try {
                    ByteArrayDataSource dataSource = new ByteArrayDataSource(part.getResource().getInputStream(true), part.getMimeType());
                    message.addAttachment(part.getFileName(), dataSource);
                } catch (Exception e) {
                    throw new ProtocolException("Something went wrong on the client side");
                }
            }
        }
    }

    private boolean isMultiPart(SmtpEvent event) {
        if (event.getParts().size() > 1) return true;
        return ThreadLocalRandom.current().nextFloat() > 0.5;
    }

    public void initializeMailSender() {
        if (mailSender != null) return;
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(getHostName());
        sender.setPort(getPort());
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");
        mailSender = sender;
    }

}
