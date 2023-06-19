package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.jpa.GelfEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private GelfEvent gelfEvent = new GelfEvent();

    @BeforeEach
    void setUp() throws UnknownHostException {
        gelfEvent.setFacility(Facility.LOCAL1);
        gelfEvent.setCreatedAt(ZonedDateTime.now());
        gelfEvent.setReceivedAt(ZonedDateTime.now());
        gelfEvent.setSentAt(ZonedDateTime.now());
        gelfEvent.setName("Gelf Message");
        gelfEvent.setGelfSeverity(Severity.INFORMATIONAL);
        gelfEvent.addPart(Body.create("shortMessage"));
        gelfEvent.addPart(Body.create("fullMessage"));
        gelfEvent.setSource(Address.create(Address.Type.HOSTNAME, InetAddress.getLocalHost().getHostName(),
                InetAddress.getLoopbackAddress().getHostAddress()));
        gelfEvent.addAttribute("a1", 1);
        gelfEvent.addAttribute("a2", "2");
        gelfEvent.setGelfSeverity(Severity.INFORMATIONAL);
    }

    @Test
    void initialize() {
        assertNotNull(gelfService);
    }

    @Test
    void sendTcp() throws IOException {
        ArgumentCaptor<net.microfalx.heimdall.protocol.jpa.GelfEvent> smtpCapture = ArgumentCaptor.forClass(net.microfalx.heimdall.protocol.jpa.GelfEvent.class);
        gelfService.handle(gelfEvent);
        Mockito.verify(gelfEventRepository).save(smtpCapture.capture());
        net.microfalx.heimdall.protocol.jpa.GelfEvent gelfEvent = smtpCapture.getValue();
        assertEquals(this.gelfEvent.getSeverity().getLevel(), gelfEvent.getLevel());
        assertEquals(this.gelfEvent.getVersion(), "1.1");
        assertEquals(this.gelfEvent.getParts().stream().toList().get(0).getResource().loadAsString(),
                gelfEvent.getShortMessage().getResource());
        assertEquals(this.gelfEvent.getParts().stream().toList().get(1).getResource().loadAsString(),
                gelfEvent.getShortMessage().getResource());
        assertEquals(this.gelfEvent.getReceivedAt().toLocalDateTime(), gelfEvent.getReceivedAt());
        assertEquals(this.gelfEvent.getSentAt().toLocalDateTime(), gelfEvent.getSentAt());
        assertEquals(this.gelfEvent.getFacility().numericalCode(), gelfEvent.getFacility());
        assertEquals("{\"a1\":1,\"a2\":\"2\"}", gelfEvent.getFields());
    }

}
