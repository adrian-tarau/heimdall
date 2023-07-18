package net.microfalx.heimdall.protocol.smtp;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachmentRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest()
@SpringBootConfiguration
@EnableAutoConfiguration()
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.heimdall"})
@Sql(statements = {"delete from protocol_smtp_attachments", "delete from protocol_smtp_events"})
public class SmtpServiceIntegrationTest {

    @Autowired
    private SmtpProperties smtpProperties;
    @Autowired
    private SmtpServer smtpServer;
    @Autowired
    private SmtpService smtpService;
    @Autowired
    private SmtpEventRepository smtpEventRepository;
    @Autowired
    private SmtpAttachmentRepository smtpAttachmentRepository;

    private JavaMailSender sender;
    private SmtpClient smtpClient = new SmtpClient();

    @BeforeEach
    void setup() {
        smtpClient.initializeMailSender();
    }

    @Test
    void sendTextEmail() {
        SimpleMailMessage message = (SimpleMailMessage) updateEmail(new SimpleMailMessage());
        message.setText("Body has a text");
        sender.send(message);
        assertEquals(1, smtpEventRepository.count());
        assertEquals(1, smtpAttachmentRepository.count());
    }

    @Test
    void sendHtmlEmail() throws MessagingException {
        sender.send(new EmailPreparator(true, false));
        assertEquals(1, smtpEventRepository.count());
        assertEquals(1, smtpAttachmentRepository.count());
    }

    @Test
    void sendAttachmentsEmail() throws MessagingException, IOException {
        sender.send(new EmailPreparator(true, true));
        assertEquals(1, smtpEventRepository.count());
        assertEquals(2, smtpAttachmentRepository.count());
    }


    private MailMessage updateEmail(MailMessage message) {
        message.setFrom("john@company.com");
        message.setTo("michael@company.com");
        message.setSubject("Test Mail");
        return message;
    }

    private MimeMessage updateEmail(MimeMessage message) throws MessagingException {
        message.setFrom("john@company.com");
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("michael@company.com"));
        message.setSubject("Test Mail");
        return message;
    }

    private void initMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(smtpProperties.getPort());

        mailSender.setUsername("test");
        mailSender.setPassword("test123");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "true");

        sender = mailSender;
    }

    private class EmailPreparator implements MimeMessagePreparator {

        private boolean withHtml;
        private boolean withAttachment;

        public EmailPreparator(boolean withHtml, boolean withAttachment) {
            this.withHtml = withHtml;
            this.withAttachment = withAttachment;
        }

        public void prepare(MimeMessage mimeMessage) throws Exception {
            updateEmail(mimeMessage);
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            if (withHtml) {
                helper.setText("<b>Hey guys</b>,<br><i>Welcome to my new home</i>", true);
            }
            if (withAttachment) {
                helper.addAttachment("application.properties", new ClassPathResource("application.properties"));
            }
        }
    }
}
