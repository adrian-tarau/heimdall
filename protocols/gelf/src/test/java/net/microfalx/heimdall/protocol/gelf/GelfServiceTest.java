package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.jpa.GelfEvent;
import net.microfalx.heimdall.protocol.jpa.GelfEventRepository;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)

public class GelfServiceTest {

    @Mock
    private PartRepository partRepository;

    @Mock
    private GelfEventRepository gelfEventRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private GelfService gelfService;

    @Spy
    private GelfConfiguration configuration;

    private GelfMessage gelfMessage= new GelfMessage();

    @BeforeEach
    void setUp() throws UnknownHostException {
        gelfMessage.setFacility(Facility.LOCAL1);
        gelfMessage.setCreatedAt(ZonedDateTime.now());
        gelfMessage.setReceivedAt(ZonedDateTime.now());
        gelfMessage.setReceivedAt(ZonedDateTime.now());
        gelfMessage.setName("Gelf Message");
        gelfMessage.setGelfMessageSeverity(Severity.INFORMATIONAL);
        gelfMessage.setBody(Body.create(gelfMessage,"Short Message"));
        gelfMessage.setBody(Body.create(gelfMessage,"Full Message"));
        gelfMessage.setSource(Address.create(InetAddress.getLocalHost().getHostName(),
                InetAddress.getLoopbackAddress().getHostAddress()));
    }

    @Test
    void initialize() {
        assertNotNull(gelfService);
    }

    @Test
    void sendTcp() throws IOException {
        ArgumentCaptor<GelfEvent> smtpCapture = ArgumentCaptor.forClass(GelfEvent.class);
        Assertions.assertNotNull(gelfService);
        gelfService.handle(gelfMessage);
        //assertEvents();
    }
}
