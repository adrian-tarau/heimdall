package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Component
public class SnmpSimulator extends ProtocolSimulator<SnmpEvent, SnmpClient> {

    private SnmpProperties configuration;

    public SnmpSimulator(ProtocolSimulatorProperties properties, SnmpProperties configuration) {
        super(properties);
        this.configuration = configuration;
    }

    @Override
    protected Address createSourceAddress() {
        return Address.create(Address.Type.HOSTNAME, "192.168." + getNextSubnet());
    }

    @Override
    protected Address createTargetAddress() {
        return Address.create(Address.Type.HOSTNAME, "192.168." + getNextSubnet());
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
