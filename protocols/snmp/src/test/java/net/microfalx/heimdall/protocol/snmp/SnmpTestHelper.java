package net.microfalx.heimdall.protocol.snmp;

import net.datafaker.Faker;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolClient;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

class SnmpTestHelper {

    private static final int START_PORT = 40000;
    private static final int PORT_RANGE = 10000;

    private static final AtomicInteger IDENTIFIER = new AtomicInteger(1);

    private ProtocolClient.Transport transport = ProtocolClient.Transport.UDP;
    private final SnmpProperties configuration;

    SnmpTestHelper(SnmpProperties configuration) {
        this.configuration = configuration;
    }

    public void setTransport(ProtocolClient.Transport transport) {
        this.transport = transport;
    }

    int getNextPort() {
        return START_PORT + ThreadLocalRandom.current().nextInt(PORT_RANGE);
    }

    void sendTrap(boolean large) throws IOException {
        SnmpClient client = new SnmpClient();
        client.setTransport(transport);
        SnmpTrap trap = new SnmpTrap();
        trap.setSource(Address.create(Address.Type.HOSTNAME, "localhost"));
        trap.setBody(Body.create(trap, "Test Message"));
        client.setPort(transport == ProtocolClient.Transport.TCP ? configuration.getTcpPort() : configuration.getUdpPort());
        if (large) {
            trap.setBody(Body.create(trap, new Faker().text().text(16000, 16000)));
        } else {
            trap.setBody(Body.create(trap, "Test Message"));
        }
        client.send(trap);
    }
}
