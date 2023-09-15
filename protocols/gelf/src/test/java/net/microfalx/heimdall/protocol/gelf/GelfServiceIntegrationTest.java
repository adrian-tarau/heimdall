package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.protocol.core.ProtocolClient;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.gelf.jpa.GelfEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest()
@SpringBootConfiguration
@EnableAutoConfiguration()
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.heimdall"})
@Sql(statements = {"delete from protocol_gelf_events", "delete from protocol_parts"})
public class GelfServiceIntegrationTest {

    @Autowired
    private GelfEventRepository gelfEventRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private GelfService gelfService;

    @Autowired
    private GelfConfiguration gelfConfiguration;

    private GelfEvent gelfEvent = new GelfEvent();

    private GelfTestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new GelfTestHelper(gelfConfiguration);
    }

    @Test
    void sendTCP() throws IOException {
        helper.setTransport(ProtocolClient.Transport.TCP);
        helper.sendLogs(false);
        await().atMost(ofSeconds(50)).until(() -> gelfEventRepository.count() > 0);
        assertEquals(1, gelfEventRepository.count());
        assertEquals(2, partRepository.count());
        assertEquals(1, addressRepository.count());
    }

    @Test
    void sendUDPSmall() throws IOException {
        helper.setTransport(ProtocolClient.Transport.UDP);
        helper.sendLogs(false);
        await().atMost(ofSeconds(5)).until(() -> gelfEventRepository.count() > 0);
        assertEquals(1, gelfEventRepository.count());
        assertEquals(2, partRepository.count());
        assertEquals(1, addressRepository.count());
    }

    @Test
    void sendUDPLarge() throws IOException {
        helper.setTransport(ProtocolClient.Transport.UDP);
        helper.sendLogs(true);
        await().atMost(ofSeconds(5)).until(() -> gelfEventRepository.count() > 0);
        assertEquals(1, gelfEventRepository.count());
        assertEquals(2, partRepository.count());
        assertEquals(1, addressRepository.count());
    }

}
