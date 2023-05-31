package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.smtp.jpa.SmtpRepository;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SmtpServiceIntegrationTest {

    @Autowired
    private SmtpConfiguration smtpConfiguration;
    @Autowired
    private SmtpServerService smtpServerService;
    @Autowired
    private SmtpService smtpService;
    @Autowired
    private SmtpRepository smtpRepository;

    private Email email = new SimpleEmail();

    @BeforeEach
    void setupClient() throws EmailException {
        email.setHostName("localhost");
        email.setSmtpPort(smtpConfiguration.getPort());
        email.setFrom("user@gmail.com");
        email.setSubject("TestMail");
        email.addTo("foo@bar.com");
    }

    @Test
    void sendSimpleEmail() throws EmailException {
        email.setMsg("Body has a text");
        email.send();
        assertEquals(1, smtpRepository.count());
    }
}
