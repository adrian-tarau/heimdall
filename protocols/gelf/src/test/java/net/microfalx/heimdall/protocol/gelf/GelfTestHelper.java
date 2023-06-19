package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.datafaker.Faker;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolClient;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class GelfTestHelper {

    private static final int START_PORT = 40000;
    private static final int PORT_RANGE = 10000;


    private ProtocolClient.Transport transport = ProtocolClient.Transport.TCP;
    private final GelfConfiguration configuration;

    GelfTestHelper(GelfConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setTransport(ProtocolClient.Transport transport) {
        this.transport = transport;
    }

    int getNextPort() {
        return START_PORT + ThreadLocalRandom.current().nextInt(PORT_RANGE);
    }

    void sendLogs(boolean large) throws IOException {
        GelfClient client = new GelfClient();
        client.setPort(transport == ProtocolClient.Transport.TCP ? configuration.getTcpPort() : configuration.getUdpPort());
        client.setTransport(transport);

        GelfEvent message = new GelfEvent();
        message.setFacility(Facility.LOCAL1);
        message.setGelfSeverity(Severity.EMERGENCY);
        if (large) {
            message.setBody(Body.create(new Faker().text().text(16000, 16000)));
        } else {
            message.setBody(Body.create("Test message"));
        }
        message.setThrowable(new IOException("Something bad happened"));
        client.send(new GelfEvent());
    }
}
