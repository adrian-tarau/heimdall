package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachmentRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.jdbc.Sql;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest()
@SpringBootConfiguration
@EnableAutoConfiguration()
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.heimdall"})
public class SmtpServiceIntegrationTest {

    @Autowired
    private SmtpConfiguration smtpConfiguration;
    @Autowired
    private SmtpServerService smtpServerService;
    @Autowired
    private SmtpService smtpService;
    @Autowired
    private SmtpEventRepository smtpEventRepository;
    @Autowired
    private SmtpAttachmentRepository smtpAttachmentRepository;

    private JavaMailSender sender;

    @BeforeEach
    void setup() {
        initMailSender();
    }

    @Test
    @Sql(statements = {"delete from protocol_smtp_attachments", "delete from protocol_smtp_events"})
    void sendTextEmail() {
        SimpleMailMessage message = (SimpleMailMessage) updateEmail(new SimpleMailMessage());
        message.setText("Body has a text");
        sender.send(message);
        assertEquals(1, smtpEventRepository.count());
        assertEquals(1, smtpAttachmentRepository.count());
    }

    /*
    @Test
    void sendHtmlEmail() throws EmailException {
        HtmlEmail email = (HtmlEmail) updateEmail(new HtmlEmail());
        email.setTextMsg("Body has a text");
        email.setHtmlMsg("Body has a HTML");
        email.send();
        assertEquals(1, smtpRepository.count());
    }

    @Test
    void sendAttachmentsEmail() throws EmailException {
        MultiPartEmail email = (MultiPartEmail) updateEmail(new MultiPartEmail());
        email.setMsg("Body has a text");
        email.send();
        assertEquals(1, smtpRepository.count());
    }

     */

    private MailMessage updateEmail(MailMessage message) {
        message.setFrom("john@company.com");
        message.setTo("michael@company.com");
        message.setSubject("Test Mail");
        return message;
    }

    private void initMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(smtpConfiguration.getPort());

        mailSender.setUsername("test");
        mailSender.setPassword("test123");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "true");

        sender = mailSender;
    }
}
