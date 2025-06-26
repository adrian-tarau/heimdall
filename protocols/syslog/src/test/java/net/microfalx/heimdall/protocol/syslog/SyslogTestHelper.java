package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

class SyslogTestHelper {

    private static final int START_PORT = 40000;
    private static final int PORT_RANGE = 10000;

    private static final AtomicInteger IDENTIFIER = new AtomicInteger(1);

    private String protocol = "tcp";
    private final SyslogProperties configuration;
    private SyslogClient syslogClient = new SyslogClient();

    SyslogTestHelper(SyslogProperties configuration) {
        this.configuration = configuration;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    int getNextPort() {
        return START_PORT + ThreadLocalRandom.current().nextInt(PORT_RANGE);
    }

    void sendLogs() throws IOException {
        send("Test 1", Severity.INFORMATIONAL, Facility.LOCAL1);
        //send(client, "Test 2", Severity.WARNING);
        // send(client, "Test 3", Severity.ERROR);
        // send(client, "Test 4", Severity.ALERT);
        // send(client, "Test 5", Severity.CRITICAL);
    }

    void send(String message, Severity severity, Facility facility) throws IOException {
        SyslogMessage event = new SyslogMessage();
        event.setFacility(facility);
        event.setSyslogSeverity(severity);
        event.setName("Syslog Message");
        event.setSource(Address.host("localhost"));
        event.setBody(Body.create(message));
        syslogClient.send(event);
    }
}
