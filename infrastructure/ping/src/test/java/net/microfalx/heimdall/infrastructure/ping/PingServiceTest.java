package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.InfrastructureEvent;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import net.microfalx.heimdall.infrastructure.core.system.ServerRepository;
import net.microfalx.heimdall.infrastructure.core.system.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PingServiceTest {

    private Service service;
    private Server server;
    private Ping ping;

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private PingRepository pingRepository;

    @Mock
    private PingResultRepository pingResultRepository;

    @Mock
    private PingPersistence persistence;

    @Mock
    private PingCache pingCache;

    @Mock
    private AsyncTaskExecutor taskExecutor;

    @Mock
    private PingScheduler pingScheduler;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private PingProperties properties;

    @Mock
    private PingHealth pingHealth;

    @Mock
    private InfrastructureService infrastructureService;

    @Mock
    private InfrastructureEvent infrastructureEvent;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private PingService pingService;

    @BeforeEach
    void setUp() throws Exception {
        service = Service.create(Service.Type.ICMP);
        server = new Server.Builder().hostname("localhost").build();
        ping = new Ping();
        pingService.afterPropertiesSet();
        pingService.onInfrastructureInitialization();
        InfrastructureEvent event= new InfrastructureEvent(null,null);
        pingService.onInfrastructureEvent(event);
    }

    @Test
    void pingWithoutRegistration() {
        when(pingCache.find(service, server)).thenReturn(null);
        assertSame(CompletablePing.class, pingService.ping(service, server).getClass());
    }

    @Test
    void pingWithRegistration() {
        when(pingCache.find(service, server)).thenReturn(ping);
        assertSame(PingExecutor.class, pingService.ping(service, server).getClass());
    }

    @Test
    void registerPing(){
        when(persistence.registerPing(anyString(),any(Service.class),any(Server.class),any(Duration.class),
                anyString())).thenReturn(true);
        assertTrue(pingService.registerPing("Ping","This is a ping",service,server,
                Duration.ofMillis(5000)));
    }

}