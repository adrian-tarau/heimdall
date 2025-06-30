package net.microfalx.heimdall.protocol.smtp;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.microfalx.resource.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;

@Component
public class SmtpGateway {

    private final SmtpGatewayProperties properties;
    private JavaMailSenderImpl mailSender;

    public SmtpGateway(SmtpGatewayProperties properties) {
        requireNonNull(properties);
        this.properties = properties;
    }

    /**
     * Loads a MIME message from the given resource.
     *
     * @param resource the resource containing the MIME message
     * @return the MIME message
     * @throws IOException   if an I/O error occurs while reading the resource
     * @throws MailException if an error occurs while creating the MIME message
     */
    public MimeMessage getMimeMessage(Resource resource) throws IOException, MailException {
        JavaMailSender mailSender = getMailSender();
        return mailSender.createMimeMessage(resource.getInputStream());
    }

    /**
     * Sends a MIME message to the configured SMTP server.
     *
     * @param resource the resource containing the MIME message
     */
    public void send(Resource resource) throws IOException, MailException {
        MimeMessage mimeMessage = getMimeMessage(resource);
        getMailSender().send(mimeMessage);
    }

    /**
     * Returns the JavaMail session used by this gateway.
     *
     * @return a non-null instance
     */
    public Session getSession() {
        return getMailSender().getSession();
    }

    private JavaMailSenderImpl getMailSender() {
        if (mailSender != null) return mailSender;
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.getHost());
        sender.setPort(properties.getPort());
        Properties props = sender.getJavaMailProperties();
        if (isNotEmpty(properties.getUserName())) {
            props.put("mail.smtp.auth", "true");
            sender.setUsername(properties.getUserName());
            sender.setPassword(properties.getPassword());
        } else {
            props.put("mail.smtp.auth", "false");
        }
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", properties.isTls());
        mailSender = sender;
        return sender;
    }
}
