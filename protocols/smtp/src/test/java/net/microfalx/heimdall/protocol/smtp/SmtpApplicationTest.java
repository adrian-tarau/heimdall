package net.microfalx.heimdall.protocol.smtp;

import net.microfalx.bootstrap.test.AbstractBootstrapApplicationTestCase;
import net.microfalx.bootstrap.test.annotation.DisableJpa;
import net.microfalx.heimdall.protocol.core.ProtocolProperties;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolSimulatorProperties;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpAttachmentRepository;
import net.microfalx.heimdall.protocol.smtp.jpa.SmtpEventRepository;
import net.microfalx.heimdall.protocol.smtp.simulator.SmtpSimulator;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;

@TestPropertySource(properties = "heimdall.protocol.simulator.enabled=true")
@ContextConfiguration(classes = {ProtocolProperties.class, ProtocolSimulatorProperties.class, SmtpProperties.class, SmtpGatewayProperties.class,
        SmtpGateway.class, SmtpSimulator.class, SmtpServer.class, SmtpService.class})
@DisableJpa
public class SmtpApplicationTest extends AbstractBootstrapApplicationTestCase {

    @MockitoBean private PartRepository partRepository;
    @MockitoBean private AddressRepository addressRepository;
    @MockitoBean private SmtpEventRepository smtpEventRepository;
    @MockitoBean private SmtpAttachmentRepository smtpAttachmentRepository;

    @MockitoBean private PlatformTransactionManager transactionManager;

    @Test
    void validate() {

    }
}
