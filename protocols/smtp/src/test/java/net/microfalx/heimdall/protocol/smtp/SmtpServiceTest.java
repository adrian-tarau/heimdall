package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
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
    private SmtpProperties configuration;

    @Mock
    private SmtpEventRepository repository;

    @InjectMocks
    private SmtpService smtpService;

    private SmtpEvent smtpEvent = new SmtpEvent();

    @BeforeEach
    void setup() {
        smtpEvent.setName("Email from Alex");
        smtpEvent.setSource(Address.email("Alex Tarau", "alex@tarau.net"));
        smtpEvent.addTarget(Address.email("Adrian Tarau", "adrian@tarau.net"));
        smtpEvent.setCreatedAt(ZonedDateTime.now());
        smtpEvent.setSentAt(ZonedDateTime.now().plusSeconds(2));
        smtpEvent.setReceivedAt(smtpEvent.getSentAt().plusSeconds(5));
        smtpEvent.setBody(Body.create("Hello"));
    }

    @Test
    void handle() throws IOException {
        ArgumentCaptor<net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent> smtpCapture = ArgumentCaptor.forClass(net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent.class);
        smtpService.accept(smtpEvent);
        verify(repository).save(smtpCapture.capture());
        net.microfalx.heimdall.protocol.smtp.jpa.SmtpEvent jpaSmtpEvent = smtpCapture.getValue();
        assertEquals(smtpEvent.getName(), jpaSmtpEvent.getSubject());
        assertEquals(smtpEvent.getSentAt().toLocalDateTime(), jpaSmtpEvent.getSentAt());
        assertEquals(smtpEvent.getReceivedAt().toLocalDateTime(), jpaSmtpEvent.getReceivedAt());
        assertEquals(smtpEvent.getSource().getName(), jpaSmtpEvent.getFrom().getName());
        assertEquals(smtpEvent.getSource().getValue(), jpaSmtpEvent.getFrom().getValue());
        assertEquals(smtpEvent.getTargets().iterator().next().getName(), jpaSmtpEvent.getTo().getName());
        assertEquals(smtpEvent.getTargets().iterator().next().getValue(), jpaSmtpEvent.getTo().getValue());
        assertEquals(smtpEvent.getBody().getResource().loadAsString(), "Hello");
    }
}