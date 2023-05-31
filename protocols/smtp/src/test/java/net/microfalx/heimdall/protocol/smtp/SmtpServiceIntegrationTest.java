package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.smtp.jpa.SmtpRepository;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

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
    private SmtpRepository smtpRepository;

    private Email email;

    @Test
    void sendSimpleEmail() throws EmailException {
        email = updateEmail(new SimpleEmail());
        email.setMsg("Body has a text");
        email.send();
        assertEquals(1, smtpRepository.count());
    }

    private Email updateEmail(Email email) throws EmailException {
        email.setHostName("localhost");
        email.setSmtpPort(smtpConfiguration.getPort());
        email.setFrom("user@gmail.com");
        email.setSubject("TestMail");
        email.addTo("foo@bar.com");
        return email;
    }
}
