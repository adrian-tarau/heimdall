package net.microfalx.heimdall.protocol.smtp;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import net.microfalx.heimdall.protocol.core.Attachment;
import net.microfalx.heimdall.protocol.core.ProtocolClient;
import net.microfalx.heimdall.protocol.core.ProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Date;
import java.util.Properties;

public class SmtpClient extends ProtocolClient<SmtpEvent> {

    @Autowired
    private SmtpConfiguration smtpConfiguration;
    private JavaMailSender mailSender;

    /**
     * Returns the default port.
     * <p>
     * Each transport protocol has a different port.
     *
     * @return the port
     */
    @Override
    protected int getDefaultPort() {
        return smtpConfiguration.getPort();
    }

    /**
     * Subclasses will implement this method to send an event.
     *
     * @param event the event
     */
    @Override
    protected void doSend(SmtpEvent event) {
        initializeMailSender();
        mailSender.send(mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setFrom(event.getSource().getValue());
            message.setTo(event.getTargets().stream().findFirst().get().getValue());
            message.setSubject(event.getName());
            message.setText(event.getBodyAsString());
            message.setSentDate(Date.from(event.getSentAt().toInstant()));
            Multipart multipart = new MimeMultipart();
            event.getParts().forEach(part -> {
                if (part instanceof Attachment) {
                    try {
                        BodyPart bodyPart = new MimeBodyPart();
                        bodyPart.setText(part.getResource().loadAsString());
                        multipart.addBodyPart(bodyPart);
                    } catch (Exception e) {
                        throw new ProtocolException("Something went wrong on the client side");
                    }
                }
            });
            mimeMessage.setContent(multipart);
        });
    }

    public void initializeMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(getHostName());
        sender.setPort(getPort());
        sender.setUsername("test");
        sender.setPassword("test123");
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "true");
        mailSender = sender;
    }

}
