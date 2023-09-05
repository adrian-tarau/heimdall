package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.*;
import net.microfalx.heimdall.protocol.snmp.mib.MibService;
import net.microfalx.resource.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static net.microfalx.lang.ConcurrencyUtils.await;

@Component
public class SnmpSimulator extends ProtocolSimulator<SnmpEvent, SnmpClient> {

    private final SnmpProperties configuration;
    private final MibService mibService;
    private final CountDownLatch latch = new CountDownLatch(1);

    public SnmpSimulator(ProtocolSimulatorProperties properties, SnmpProperties configuration, MibService mibService) {
        super(properties);
        this.configuration = configuration;
        this.mibService = mibService;
    }

    @Override
    protected Address createSourceAddress() {
        return Address.create(Address.Type.HOSTNAME, "192.168." + getRandomSubnet());
    }

    @Override
    protected Address createTargetAddress() {
        return Address.create(Address.Type.HOSTNAME, "192.168." + getRandomSubnet());
    }

    @Override
    protected void initializeData() {
        mibService.getTaskExecutor().execute(new LoadMibsWorker());
    }

    @Override
    protected Collection<ProtocolClient<SnmpEvent>> createClients() {
        SnmpClient udpClient = new SnmpClient();
        udpClient.setPort(configuration.getUdpPort());
        return Arrays.asList(udpClient);
    }

    @Override
    protected void simulate(ProtocolClient<SnmpEvent> client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        await(latch);
        SnmpEvent trap = new SnmpEvent();
        trap.setSource(sourceAddress);
        trap.addTarget(targetAddress);
        trap.setBody(Body.create(getRandomText()));
        client.send(trap);
    }

    private class LoadMibsWorker implements Runnable {

        @Override
        public void run() {
            try {
                mibService.loadModules(ClassPathResource.directory("simulator/snmp/mib"));
            } finally {
                latch.countDown();
            }
        }
    }
}
