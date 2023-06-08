package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.protocol.core.ProtocolClient;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class GelfTestHelper {

    private static final int START_PORT = 40000;
    private static final int PORT_RANGE = 10000;

    private static final AtomicInteger IDENTIFIER = new AtomicInteger(1);

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

    void sendLogs() throws IOException {
        GelfClient client = new GelfClient();
        client.setTransport(transport);
        client.setPort(transport == ProtocolClient.Transport.TCP ? configuration.getTcpPort() : configuration.getUdpPort());
        client.setMessage("Test message");
        client.setThrowable(new IOException("Something bad happened"));
        GelfMessage message = new GelfMessage();
        client.send(new GelfMessage());
    }
}
