package net.microfalx.heimdall.protocol.syslog;

import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.jpa.SyslogEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static net.microfalx.lang.ThreadUtils.sleepSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest()
@SpringBootConfiguration
@EnableAutoConfiguration()
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.heimdall"})
@Sql(statements = {"delete from protocol_syslog_events"})
public class SyslogServiceIntegrationTest {

    @Autowired
    private SyslogEventRepository syslogEventRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private SyslogService syslogService;

    @Autowired
    private SyslogConfiguration configuration = new SyslogConfiguration();

    private SyslogMessage syslogMessage = new SyslogMessage();

    private SyslogTestHelper helper;

    @BeforeEach
    void setup() {
        helper = new SyslogTestHelper(configuration);
        //configuration.setTcpPort(helper.getNextPort());
        //configuration.setUdpPort(helper.getNextPort());
    }

    @Test
    void sendTcp() throws IOException {
        helper.setProtocol("tcp");
        helper.sendLogs();
        sleepSeconds(1);
        assertEquals(1, syslogEventRepository.count());
        assertEquals(1, partRepository.count());
        assertEquals(1, addressRepository.count());
    }

    @Test
    void sendUdp() throws IOException {
        helper.setProtocol("udo");
        helper.sendLogs();
        sleepSeconds(1);
        assertEquals(1, syslogEventRepository.count());
        assertEquals(1, partRepository.count());
        assertEquals(1, addressRepository.count());
    }

}
