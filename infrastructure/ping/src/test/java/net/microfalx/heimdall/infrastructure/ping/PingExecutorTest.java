package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PingExecutorTest {

    private PingExecutor pingExecutor;
    private Ping ping;
    private Service service;
    private Server server;
    @Mock
    private InfrastructureService infrastructureService;
    @Mock
    private PingRepository pingRepository;
    @Mock
    PingResultRepository pingResultRepository;
    @InjectMocks
    private PingPersistence persistence;

    private Random random = ThreadLocalRandom.current();

    @BeforeEach
    void setup() {
        server = new Server.Builder().hostname("localhost").build();
        service = Service.create(Service.Type.ICMP);
        ping = new Ping();
    }

    @Test
    void executePingOverICMPWithIcmpEnabled() {
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingOverICMPWithIcmpDisable() {
        server = new Server.Builder().hostname("localhost").icmp(false).build();
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.CANCEL, result.getStatus());
        assertNotNull(result.getDuration());
        assertNotNull(result.getStartedAt());
        assertNotNull(result.getEndedAt());
    }

    @Test
    void executePingTCPWithSuccess() throws IOException {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().connectionTimeout(Duration.ofMillis(5)).type(Service.Type.TCP).port(port).build();
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(service.getPort()));
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        serverSocket.accept();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.SUCCESS, result.getStatus());
    }

    @Test
    void executePingTCPWithFailure() {
        int port = 49000 + random.nextInt(10000);
        service = new Service.Builder().connectionTimeout(Duration.ofMillis(5)).type(Service.Type.TCP).port(port).build();
        pingExecutor = new PingExecutor(ping, service, server, persistence, infrastructureService);
        net.microfalx.heimdall.infrastructure.api.Ping result = pingExecutor.execute();
        assertNotNull(result);
        assertEquals(net.microfalx.heimdall.infrastructure.api.Ping.Status.FAILURE, result.getStatus());
    }
}