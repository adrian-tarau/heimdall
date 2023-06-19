package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SnmpSimulator extends ProtocolSimulator<SnmpEvent, SnmpClient> {

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
    protected Collection<ProtocolClient<SnmpEvent>> createClients() {
        SnmpClient udpClient = new SnmpClient();
        udpClient.setPort(configuration.getUdpPort());
        return Arrays.asList(udpClient);
    }

    @Override
    protected void simulate(ProtocolClient<SnmpEvent> client, Address address, int index) throws IOException {
        SnmpEvent trap = new SnmpEvent();
        trap.setSource(Address.create(Address.Type.HOSTNAME, "localhost"));
        trap.addTarget(Address.create(Address.Type.HOSTNAME, client.getHostName()));
        trap.setBody(Body.create(getNextBody()));
        client.send(trap);
    }
}
