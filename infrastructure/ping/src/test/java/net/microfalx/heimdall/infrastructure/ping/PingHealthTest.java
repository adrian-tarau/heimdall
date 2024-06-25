package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Ping;
import net.microfalx.heimdall.infrastructure.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PingHealthTest {

    private Service service;
    private Server server;

    @Mock
    private Ping ping;

    @Spy
    private PingProperties properties;

    @InjectMocks
    private PingHealth health;

    @BeforeEach
    void setUp() {
        service = Service.create(Service.Type.ICMP);
        server = new Server.Builder().hostname("localhost").build();
        mockPing(Status.NA);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void noPingRegistration() {
        when(ping.getStatus()).thenReturn(Status.NA);
        assertEquals(Status.NA, health.getStatus(service, server));
        assertEquals(Health.NA, health.getHealth(service, server));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void withPingRegistrationWithOneSuccessStatus() {
        when(ping.getStatus()).thenReturn(Status.L3OK);
        health.registerPing(ping);
        assertEquals(1, health.getPings().values().iterator().next().size());
        assertEquals(Status.L3OK, health.getStatus(service, server));
        assertEquals(Health.HEALTHY, health.getHealth(service, server));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void withPingRegistrationWithFullHealthyStatus() {
        when(ping.getStatus()).thenReturn(Status.L3OK);
        registerFullWindowPings();
        mockPing(Status.L4OK);
        health.registerPing(ping);
        assertEquals(properties.getWindowSize(), health.getPings().values().iterator().next().size());
        assertEquals(Status.L4OK, health.getStatus(service, server));
        assertEquals(Health.HEALTHY, health.getHealth(service, server));
    }


    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void withPingRegistrationWithOneFailureStatus() {
        when(ping.getStatus()).thenReturn(Status.L3CON);
        health.registerPing(ping);
        assertEquals(1, health.getPings().values().iterator().next().size());
        assertEquals(Status.L3CON, health.getStatus(service, server));
        assertEquals(Health.NA, health.getHealth(service, server));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void withPingRegistrationWithFullAndDegradedStatus() {
        when(ping.getStatus()).thenReturn(Status.L3OK);
        registerFullWindowPings();
        mockPing(Status.L3CON);
        health.registerPing(ping);
        assertEquals(properties.getWindowSize(), health.getPings().values().iterator().next().size());
        assertEquals(Status.L3CON, health.getStatus(service, server));
        assertEquals(Health.DEGRADED, health.getHealth(service, server));
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void withPingRegistrationWithFullAndUnhealthyStatus() {
        when(ping.getStatus()).thenReturn(Status.L4TOUT);
        registerFullWindowPings();
        mockPing(Status.L7TOUT);
        health.registerPing(ping);
        mockPing(Status.L3CON);
        health.registerPing(ping);
        assertEquals(properties.getWindowSize(), health.getPings().values().iterator().next().size());
        assertEquals(Status.L3CON, health.getStatus(service, server));
        assertEquals(Health.UNHEALTHY, health.getHealth(service, server));
    }

    private void registerFullWindowPings() {
        for (int i = 0; i < properties.getWindowSize(); i++) {
            health.registerPing(ping);
        }
    }

    private void mockPing(Status status) {
        ping = mock(Ping.class);
        when(ping.getId()).thenReturn(PingUtils.getId(service, server));
        when(ping.getStatus()).thenReturn(status);
    }

}