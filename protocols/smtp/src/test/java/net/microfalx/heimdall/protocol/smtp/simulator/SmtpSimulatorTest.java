package net.microfalx.heimdall.protocol.smtp.simulator;

import net.microfalx.bootstrap.mail.MailProperties;
import net.microfalx.bootstrap.mail.MailService;
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

    @Mock
    private SmtpClient client;
    @Mock
    private Address sourceAddress;
    @Mock
    private Address targetAddress;
    @Mock
    private SmtpService service;
    @Mock
    private MailService mailService;

    @Spy
    private ProtocolSimulatorProperties simulatorProperties = new ProtocolSimulatorProperties();
    @Spy
    private SmtpProperties smtpProperties = new SmtpProperties();
    @Spy
    private MailProperties mailProperties = new MailProperties();

    private SmtpSimulator simulator;

    @BeforeEach
    void setup() throws Exception {
        simulatorProperties.setEnabled(true);
        SmtpServer server = new SmtpServer(smtpProperties, mailService);
        simulator = new SmtpSimulator(simulatorProperties, smtpProperties, server);
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