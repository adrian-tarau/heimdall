package net.microfalx.heimdall.protocol.gelf;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.datafaker.Faker;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.ProtocolSimulator;
import net.microfalx.heimdall.protocol.core.ProtocolSimulatorProperties;
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
        return Address.create(Address.Type.HOSTNAME, getRandomDomainOrIp());
    }

    @Override
    protected Address createTargetAddress() {
        return Address.create(Address.Type.HOSTNAME, getRandomDomainOrIp());
    }

    @Override
    protected Collection<GelfClient> createClients() {
        GelfClient client = new GelfClient();
        return Arrays.asList(client);
    }


    @Override
    protected void simulate(GelfClient client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        GelfEvent message = new GelfEvent();
        message.setBody(Body.create(getRandomText()));
        message.setGelfSeverity(getRandomEnum(Severity.class));
        message.setFacility(getRandomEnum(Facility.class));
        message.setSource(sourceAddress);
        message.addTarget(targetAddress);
        updateAttributes(message);
        if (random.nextFloat() > 0.8) message.setThrowable(new IOException("Something is wrong"));
        client.send(message);
    }

    private void updateAttributes(GelfEvent message) {
        Faker faker = getFaker();
        message.addAttribute("os", faker.computer().operatingSystem());
        message.addAttribute("platform", faker.computer().platform());
        message.addAttribute("domain", faker.domain().fullDomain("net.microfalx.simulator"));
    }
}
