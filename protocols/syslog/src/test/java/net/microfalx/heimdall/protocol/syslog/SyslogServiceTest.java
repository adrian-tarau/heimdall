package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEvent;
import net.microfalx.heimdall.protocol.syslog.jpa.SyslogEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SyslogServiceTest {

    @InjectMocks
    private SyslogService syslogService;
    @Mock
    private SyslogEventRepository repository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    PartRepository partRepository;

    @Spy
    private SyslogProperties configuration;

    private SyslogMessage message = new SyslogMessage();

    @BeforeEach
    void setMessage() throws UnknownHostException {
        message.setFacility(Facility.ALERT);
        Address address = Address.create(Address.Type.HOSTNAME, InetAddress.getLocalHost().getHostName(),
                InetAddress.getLocalHost().getHostAddress());
        message.setSource(address);
        message.setBody(Body.create("Body has a text"));
        message.setSyslogSeverity(Severity.ALERT);
        message.setReceivedAt(ZonedDateTime.now());
        message.setCreatedAt(ZonedDateTime.now());
        message.setName("");
        message.setSentAt(ZonedDateTime.now());

    }

    @Test
    void handle() throws IOException {
        ArgumentCaptor<SyslogEvent> smtpCapture = ArgumentCaptor.forClass(SyslogEvent.class);
        Assertions.assertNotNull(syslogService);
        syslogService.accept(message);
        verify(repository).save(smtpCapture.capture());
        SyslogEvent syslogEvent = smtpCapture.getValue();
        assertEquals(message.getSource().getName(), syslogEvent.getAddress().getName());
        assertEquals(message.getSource().getValue(), syslogEvent.getAddress().getValue());
        assertEquals(message.getFacility().numericalCode(), syslogEvent.getFacility());
        assertEquals(message.getSyslogSeverity().numericalCode(), syslogEvent.getSeverity());
        assertEquals(message.getSentAt().toLocalDateTime(), syslogEvent.getSentAt());
        assertEquals(message.getReceivedAt().toLocalDateTime(), syslogEvent.getReceivedAt());
        assertEquals(message.getBody().getResource().loadAsString(), "Body has a text");
    }
}