package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.datafaker.Faker;
import net.microfalx.heimdall.protocol.core.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Component
public class GelfSimulator extends ProtocolSimulator<GelfEvent, GelfClient> {

    public GelfSimulator(ProtocolSimulatorProperties properties) {
        super(properties);
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
    protected Collection<ProtocolClient<GelfEvent>> createClients() {
        GelfClient client = new GelfClient();
        return Arrays.asList(client);
    }


    @Override
    protected void simulate(ProtocolClient<GelfEvent> client, Address address, int index) throws IOException {
        GelfEvent message = new GelfEvent();
        message.setBody(Body.create(getNextBody()));
        message.setGelfSeverity(getNextEnum(Severity.class));
        message.setFacility(getNextEnum(Facility.class));
        message.setSource(address);
        message.addTarget(Address.create(Address.Type.HOSTNAME, client.getHostName()));
        updateAttributes(message);
        if (random.nextFloat() > 0.8) message.setThrowable(new IOException("Something is wrong"));
        client.send(message);
    }

    private void updateAttributes(GelfEvent message) {
        Faker faker = new Faker();
        message.addAttribute("OS", faker.computer().operatingSystem());
        message.addAttribute("Platform", faker.computer().platform());
        message.addAttribute("Domain", faker.domain().fullDomain("spirent.com"));
    }
}
