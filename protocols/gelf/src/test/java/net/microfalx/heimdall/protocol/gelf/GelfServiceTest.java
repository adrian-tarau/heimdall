package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.jpa.GelfEvent;
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

    private GelfMessage gelfMessage = new GelfMessage();

    @BeforeEach
    void setUp() throws UnknownHostException {
        gelfMessage.setFacility(Facility.LOCAL1);
        gelfMessage.setCreatedAt(ZonedDateTime.now());
        gelfMessage.setReceivedAt(ZonedDateTime.now());
        gelfMessage.setSentAt(ZonedDateTime.now());
        gelfMessage.setName("Gelf Message");
        gelfMessage.setGelfSeverity(Severity.INFORMATIONAL);
        gelfMessage.addPart(Body.create(gelfMessage, "shortMessage"));
        gelfMessage.addPart(Body.create(gelfMessage, "fullMessage"));
        gelfMessage.setSource(Address.create(InetAddress.getLocalHost().getHostName(),
                InetAddress.getLoopbackAddress().getHostAddress()));
        gelfMessage.addAttribute("a1", 1);
        gelfMessage.addAttribute("a2", "2");
        gelfMessage.setGelfSeverity(Severity.INFORMATIONAL);
    }

    @Test
    void initialize() {
        assertNotNull(gelfService);
    }

    @Test
    void sendTcp() throws IOException {
        ArgumentCaptor<GelfEvent> smtpCapture = ArgumentCaptor.forClass(GelfEvent.class);
        gelfService.handle(gelfMessage);
        Mockito.verify(gelfEventRepository).save(smtpCapture.capture());
        GelfEvent gelfEvent = smtpCapture.getValue();
        assertEquals(gelfMessage.getSeverity().getLevel(), gelfEvent.getLevel());
        assertEquals(gelfEvent.getVersion(),"1.1");
        assertEquals(gelfMessage.getParts().stream().toList().get(0).getResource().loadAsString(),
                gelfEvent.getShort_attachment_id().getResource());
        assertEquals(gelfMessage.getParts().stream().toList().get(1).getResource().loadAsString(),
                gelfEvent.getLong_attachment_id().getResource());
        assertEquals(gelfMessage.getReceivedAt().toLocalDateTime(),gelfEvent.getReceivedAt());
        assertEquals(gelfMessage.getSentAt().toLocalDateTime(),gelfEvent.getSentAt());
        assertEquals(gelfMessage.getFacility().numericalCode(),gelfEvent.getFacility());
        assertEquals("{\"a1\":1,\"a2\":\"2\"}",gelfEvent.getFields());
    }

}
