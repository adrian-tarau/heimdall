package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SnmpSimulator extends ProtocolSimulator<SnmpTrap, SnmpClient> {

    private static final AtomicInteger SOURCE_INDEX_GENERATOR = new AtomicInteger(1);

    private SnmpProperties configuration;

    public SnmpSimulator(ProtocolSimulatorProperties properties, SnmpProperties configuration) {
        super(properties);
        this.configuration = configuration;
    }

    @Override
    protected Address createAddress() {
        return Address.create(Address.Type.HOSTNAME, "192.168.2." + SOURCE_INDEX_GENERATOR.getAndIncrement());
    }

    @Override
    protected Collection<ProtocolClient<SnmpTrap>> createClients() {
        SnmpClient udpClient = new SnmpClient();
        udpClient.setPort(configuration.getUdpPort());
        return Arrays.asList(udpClient);
    }

    @Override
    protected void simulate(ProtocolClient<SnmpTrap> client, Address address, int index) throws IOException {
        SnmpTrap trap = new SnmpTrap();
        trap.setSource(Address.create(Address.Type.HOSTNAME, "localhost"));
        trap.setBody(Body.create(trap, "Test Message"));
        client.send(trap);
    }
}
