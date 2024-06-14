package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.core.Server;
import net.microfalx.heimdall.infrastructure.core.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PingSchedulerTest {

    @Mock
    private PingRepository pingRepository;

    @Mock
    private PingResultRepository pingResultRepository;

    @Mock
    private PingPersistence pingPersistence;

    @Mock
    private PingCache cache;

    @Mock
    private InfrastructureService infrastructureService;

    @Mock
    private AsyncTaskExecutor taskExecutor;

    @InjectMocks
    private PingScheduler pingScheduler;

    private net.microfalx.heimdall.infrastructure.api.Service service;
    private net.microfalx.heimdall.infrastructure.api.Server server;
    private List<net.microfalx.heimdall.infrastructure.ping.Ping> pings;

    private static final AtomicInteger idGenerator = new AtomicInteger(1);

    @BeforeEach
    void setUp() {
        setupInfrastructure();
        createPings();
        setupCache();
    }

    @Test
    void allPingsScheduled() {
        pingScheduler.run();
        verify(cache).getPings();
        verify(taskExecutor, times(3)).submit(any(Runnable.class));
    }

    @Test
    void runPingSchedule() {
        pingScheduler.run();

    }

    private net.microfalx.heimdall.infrastructure.ping.Ping createPing(String hostName, boolean icmp,
                                                                       int port, int interval) {
        Server jpaServer = new Server();
        jpaServer.setId(idGenerator.getAndIncrement());
        jpaServer.setName("Server");
        jpaServer.setNaturalId(hostName);
        jpaServer.setHostname(hostName);
        jpaServer.setIcmp(icmp);

        Service jpaService = new Service();
        jpaService.setId(idGenerator.getAndIncrement());
        jpaService.setName("Service");
        jpaService.setNaturalId(service.getId());
        jpaService.setType(service.getType());
        jpaService.setPort(port);
        jpaService.setUsername("");
        jpaService.setPassword("");
        jpaService.setToken("");

        net.microfalx.heimdall.infrastructure.ping.Ping jpaPing = new Ping();
        jpaPing.setId(idGenerator.getAndIncrement());
        jpaPing.setService(jpaService);
        jpaPing.setServer(jpaServer);
        jpaPing.setInterval(interval);
        return jpaPing;
    }

    private void setupCache() {
        when(cache.getPings()).thenReturn(pings);
    }

    private void setupInfrastructure() {
        service = new net.microfalx.heimdall.infrastructure.api.Service.Builder().port(80).build();
        server = new net.microfalx.heimdall.infrastructure.api.Server.Builder().hostname("host1").icmp(true).build();
    }

    private void createPings() {
        pings = new ArrayList<>();
        pings.add(createPing("host1", true, 80, 2_000));
        pings.add(createPing("host2", false, 80, 5_000));
        pings.add(createPing("host3", true, 80, 10_000));
    }

}