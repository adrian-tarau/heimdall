package net.microfalx.heimdall.protocol.smtp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SmtpServerServiceTest {

    @Mock
    private SmtpService smtpService;

    @Mock
    private SmtpConfiguration configuration;

    @InjectMocks
    private SmtpServerService serverService;

    @BeforeEach
    void setup() {
        when(configuration.getPort()).thenReturn(2525);
        when(configuration.getConnectionTimeout()).thenReturn(5000);
        when(configuration.getMaxConnections()).thenReturn(100);
        when(configuration.getMaxRecipients()).thenReturn(10);
    }

    @Test
    void initialize() {
        assertNotNull(serverService);
        serverService.initialize();
    }

}