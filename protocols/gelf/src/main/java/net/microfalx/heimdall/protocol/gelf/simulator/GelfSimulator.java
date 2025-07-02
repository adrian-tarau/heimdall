package net.microfalx.heimdall.protocol.gelf.simulator;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.datafaker.Faker;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Body;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolDataSetFactory;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolSimulator;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolSimulatorProperties;
import net.microfalx.heimdall.protocol.gelf.GelfClient;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.heimdall.protocol.gelf.GelfProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@Component
public class GelfSimulator extends ProtocolSimulator<GelfEvent, GelfClient> {

    private final GelfProperties properties;
    private Collection<GelfDataSet> dataSets;
    private Iterator<GelfDataSet> nextDataSet;

    public GelfSimulator(ProtocolSimulatorProperties simulatorProperties, GelfProperties gelfProperties) {
        super(simulatorProperties);
        this.properties = gelfProperties;
    }

    @Override
    protected Event.Type getEventType() {
        return Event.Type.GELF;
    }

    @Override
    protected Address createSourceAddress() {
        return Address.create(Address.Type.HOSTNAME, getRandomDomainOrIp(true));
    }

    @Override
    protected Address createTargetAddress() {
        return Address.create(Address.Type.HOSTNAME, getRandomDomainOrIp(false));
    }

    @Override
    protected Collection<GelfClient> createClients() {
        GelfClient client = new GelfClient();
        return Arrays.asList(client);
    }

    @Override
    public boolean isEnabled() {
        if (super.isEnabled()) {
            return true;
        } else {
            return properties.isSimulatorEnabled();
        }
    }

    @Override
    protected void simulate(GelfClient client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        GelfEvent event = new GelfEvent();
        if (shouldUseExternalDataSets()) {
            getDataSet().update(event, sourceAddress, targetAddress);
        } else {
            event.setBody(Body.create(getRandomText()));
            event.setGelfSeverity(getRandomEnum(Severity.class));
            event.setFacility(getRandomEnum(Facility.class));
            event.setSource(sourceAddress);
            event.addTarget(targetAddress);
            updateAttributes(event);
            if (random.nextFloat() > 0.8) event.setThrowable(new IOException("Something is wrong"));
        }
        client.send(event);
    }

    private void updateAttributes(GelfEvent message) {
        Faker faker = getFaker();
        message.add("os", faker.computer().operatingSystem());
        message.add("platform", faker.computer().platform());
        message.add("domain", faker.domain().fullDomain("net.microfalx.simulator"));
    }

    private GelfDataSet getDataSet() {
        if (dataSets == null) {
            dataSets = new ArrayList<>();
            Collection<ProtocolDataSetFactory> factories = ProtocolDataSetFactory.getFactories();
            for (ProtocolDataSetFactory factory : factories) {
                if (factory instanceof GelfDataSet.Factory) {
                    dataSets.add(((GelfDataSet.Factory) factory).createDataSet());
                }
            }
        }
        if (nextDataSet == null || !nextDataSet.hasNext()) {
            nextDataSet = dataSets.iterator();
        }
        if (nextDataSet.hasNext()) {
            return nextDataSet.next();
        } else {
            throw new IllegalStateException("No more data sets available");
        }
    }
}
