package net.microfalx.heimdall.protocol.smtp.simulator;

import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolSimulatorProperties;
import net.microfalx.heimdall.protocol.smtp.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpSimulatorTest {

    @Mock private SmtpClient client;
    @Mock private Address sourceAddress;
    @Mock private Address targetAddress;
    @Mock private SmtpService service;

    @Spy private ProtocolSimulatorProperties simulatorProperties = new ProtocolSimulatorProperties();
    @Spy private SmtpProperties smtpProperties = new SmtpProperties();
    @Spy private SmtpGatewayProperties gatewayProperties = new SmtpGatewayProperties();

    private SmtpSimulator simulator;

    @BeforeEach
    void setup() throws Exception {
        simulatorProperties.setEnabled(true);
        SmtpGateway gateway = new SmtpGateway(gatewayProperties);
        SmtpServer server = new SmtpServer(smtpProperties, gateway);
        simulator = new SmtpSimulator(simulatorProperties, smtpProperties, gateway, server);
    }

    @Test
    void simulateFakeData() throws IOException {
        simulatorProperties.setUseExternalDataSets(false);
        simulator.simulate(client, sourceAddress, targetAddress, 1);
        verify(client).send(any(SmtpEvent.class));
    }

    @Test
    void simulateReal() throws IOException {
        simulatorProperties.setUseExternalDataSets(true);
        simulator.simulate(client, sourceAddress, targetAddress, 1);
        verify(client).send(any(SmtpEvent.class));
    }
}