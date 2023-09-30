package net.microfalx.heimdall.protocol.syslog;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;

@Component
public class SyslogSimulator extends ProtocolSimulator<SyslogMessage, SyslogClient> {

    private SyslogConfiguration syslogConfiguration;

    public SyslogSimulator(ProtocolSimulatorProperties properties, SyslogConfiguration syslogConfiguration) {
        super(properties);
        this.syslogConfiguration = syslogConfiguration;
    }

    /**
     * Invoked to create a list of target addresses.
     *
     * @return an address
     */
    @Override
    protected Address createSourceAddress() {
        return Address.create(Address.Type.HOSTNAME, getRandomDomainOrIp());
    }

    /**
     * Invoked to create a list of source addresses.
     *
     * @return an address
     */
    @Override
    protected Address createTargetAddress() {
        return Address.create(Address.Type.HOSTNAME, getRandomDomainOrIp());
    }

    /**
     * Invoked to create the client.
     *
     * @return a non-null instance
     */
    @Override
    protected Collection<SyslogClient> createClients() {
        SyslogClient syslogClient = new SyslogClient();
        syslogClient.setPort(syslogConfiguration.getUdpPort());
        syslogClient.setTransport(ProtocolClient.Transport.UDP);
        return Arrays.asList(syslogClient);
    }

    /**
     * Simulates an event.
     *
     * @param client        the client
     * @param sourceAddress the source address
     * @param targetAddress the target address
     * @param index         the event index
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void simulate(SyslogClient client, Address sourceAddress, Address targetAddress, int index) throws IOException {
        SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage.setSyslogSeverity(getRandomEnum(Severity.class));
        syslogMessage.setFacility(getRandomEnum(Facility.class));
        syslogMessage.setSource(sourceAddress);
        syslogMessage.addTarget(targetAddress);
        syslogMessage.setBody(Body.create(getRandomText()));
        syslogMessage.setCreatedAt(ZonedDateTime.now());
        syslogMessage.setSentAt(ZonedDateTime.now());
        client.send(syslogMessage);
    }
}
