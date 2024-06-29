package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.core.system.Server;
import net.microfalx.heimdall.infrastructure.core.system.Service;
import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PingCacheTest {

    @Mock
    private PingRepository pingRepository;

    @InjectMocks
    private PingCache pingCache;

    private List<Ping> pingList;

    @BeforeEach
    void setUp() {
        createPings();
        when(pingRepository.findByActive(anyBoolean())).thenReturn(pingList);
        pingCache.reload();
    }

    @Test
    void reload() {
        verify(pingRepository).findByActive(anyBoolean());
        assertEquals(3, pingCache.getPings().size());
    }

    @Test
    void findWithMissingService() {
        net.microfalx.heimdall.infrastructure.api.Service service = new
                net.microfalx.heimdall.infrastructure.api.Service.Builder().port(1234).build();
        net.microfalx.heimdall.infrastructure.api.Server server = new
                net.microfalx.heimdall.infrastructure.api.Server.Builder().hostname("host1").icmp(true).build();
        assertNull(pingCache.find(service, server));
        assertEquals(0, getInternalCache().size());
    }

    @Test
    void findWithCorrectServerAndService() {
        net.microfalx.heimdall.infrastructure.api.Service service = new
                net.microfalx.heimdall.infrastructure.api.Service.Builder()
                .type(net.microfalx.heimdall.infrastructure.api.Service.Type.HTTP).port(60).build();
        net.microfalx.heimdall.infrastructure.api.Server server = new
                net.microfalx.heimdall.infrastructure.api.Server.Builder().hostname("host1").icmp(true).build();
        assertNotNull(pingCache.find(service, server));
        assertEquals(1, getInternalCache().size());
    }

    private Map<?, ?> getInternalCache() {
        return Reflect.on(pingCache).field("cachePingsByServiceAndServer").get();
    }

    private Ping createPing(String hostName, boolean icmp, int port) {
        Server jpaServer = new Server();
        jpaServer.setName("Server");
        jpaServer.setNaturalId(hostName);
        jpaServer.setHostname(hostName);
        jpaServer.setIcmp(icmp);

        net.microfalx.heimdall.infrastructure.api.Service service = new net.microfalx.heimdall.infrastructure.api.Service.Builder()
                .type(net.microfalx.heimdall.infrastructure.api.Service.Type.HTTP)
                .port(port).build();
        Service jpaService = new Service();
        jpaService.setName("Service");
        jpaService.setNaturalId(service.getId());
        jpaService.setType(service.getType());
        jpaService.setPort(port);
        jpaService.setUsername("");
        jpaService.setPassword("");
        jpaService.setToken("");
        Ping jpaPing = new Ping();
        jpaPing.setService(jpaService);
        jpaPing.setServer(jpaServer);
        return jpaPing;
    }

    private void createPings() {
        pingList = new ArrayList<>();
        pingList.add(createPing("host1", true, 80));
        pingList.add(createPing("host2", false, 80));
        pingList.add(createPing("host3", true, 80));
    }
}