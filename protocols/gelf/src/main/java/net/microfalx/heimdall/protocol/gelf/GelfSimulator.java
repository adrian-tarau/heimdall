package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GelfSimulator extends ProtocolSimulator<GelfMessage, GelfClient> {

    private static final AtomicInteger SOURCE_INDEX_GENERATOR = new AtomicInteger(1);

    public GelfSimulator(ProtocolSimulatorProperties properties) {
        super(properties);
    }

    @Override
    protected Address createAddress() {
        return Address.create(Address.Type.HOSTNAME, "192.168.1." + SOURCE_INDEX_GENERATOR.getAndIncrement());
    }

    @Override
    protected Collection<ProtocolClient<GelfMessage>> createClients() {
        GelfClient client = new GelfClient();
        return Arrays.asList(client);
    }

    @Override
    protected void simulate(ProtocolClient<GelfMessage> client, Address address, int index) throws IOException {
        GelfMessage message = new GelfMessage();
        message.setBody(Body.create(message, "Test Message " + index));
        message.setGelfSeverity(getNextEnum(Severity.class));
        message.setFacility(getNextEnum(Facility.class));
        message.setSource(address);
        message.addTarget(Address.create(Address.Type.HOSTNAME, client.getHostName()));
        if (random.nextFloat() > 0.8) message.setThrowable(new IOException("Something is wrong"));
        client.send(message);
    }
}
