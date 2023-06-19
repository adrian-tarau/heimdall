package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpServiceTest {

    @Mock
    private SmtpConfiguration configuration;

    @Mock
    private SmtpEventRepository repository;

    @InjectMocks
    private SmtpService smtpService;

    private Email email = new Email();

    @BeforeEach
    void setup() {
        email.setName("Email from Alex");
        email.setSource(Address.create(Address.Type.EMAIL, "Alex Tarau", "alex@tarau.net"));
        email.addTarget(Address.create(Address.Type.EMAIL, "Adrian Tarau", "adrian@tarau.net"));
        email.setCreatedAt(ZonedDateTime.now());
        email.setSentAt(ZonedDateTime.now().plusSeconds(2));
        email.setReceivedAt(email.getSentAt().plusSeconds(5));
        email.setBody(Body.create("Hello"));
    }

    @Test
    void handle() throws IOException {
        ArgumentCaptor<SmtpEvent> smtpCapture = ArgumentCaptor.forClass(SmtpEvent.class);
        smtpService.handle(email);
        verify(repository).save(smtpCapture.capture());
        SmtpEvent smtpEvent = smtpCapture.getValue();
        assertEquals(email.getName(), smtpEvent.getSubject());
        assertEquals(email.getSentAt().toLocalDateTime(), smtpEvent.getSentAt());
        assertEquals(email.getReceivedAt().toLocalDateTime(), smtpEvent.getReceivedAt());
        assertEquals(email.getSource().getName(), smtpEvent.getFrom().getName());
        assertEquals(email.getSource().getValue(), smtpEvent.getFrom().getValue());
        assertEquals(email.getTargets().iterator().next().getName(), smtpEvent.getTo().getName());
        assertEquals(email.getTargets().iterator().next().getValue(), smtpEvent.getTo().getValue());
        assertEquals(email.getBody().getResource().loadAsString(), "Hello");
    }
}