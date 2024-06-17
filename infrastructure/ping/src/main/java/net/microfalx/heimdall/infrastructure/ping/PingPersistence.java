package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Ping;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.core.ServerRepository;
import net.microfalx.heimdall.infrastructure.core.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
class PingPersistence {

    @Autowired
    private PingRepository pingRepository;

    @Autowired
    private PingResultRepository pingResultRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Persists the ping and its result in the database.
     *
     * @param ping the ping
     */
    void persist(net.microfalx.heimdall.infrastructure.ping.Ping jpaPing, Ping ping) {
        PingResult pingResult = new PingResult();
        pingResult.setService(jpaPing.getService());
        pingResult.setServer(jpaPing.getServer());
        pingResult.setStatus(ping.getStatus());
        pingResult.setStartedAt(ping.getStartedAt());
        pingResult.setEndedAt(ping.getEndedAt());
        pingResult.setErrorMessage(ping.getErrorMessage());
        pingResult.setDuration(ping.getDuration());
        pingResult.setPing(jpaPing);
        pingResultRepository.save(pingResult);
    }

    /**
     * Registers a ping.
     *
     * @param service  the service
     * @param server   the server
     * @param interval the interval
     */
    public void registerPing(net.microfalx.heimdall.infrastructure.api.Service service, Server server, Duration interval) {
        if (pingRepository.hasPing(server.getId(), service.getId())) return;
        net.microfalx.heimdall.infrastructure.ping.Ping ping = new net.microfalx.heimdall.infrastructure.ping.Ping();
        ping.setActive(true);
        ping.setServer(serverRepository.findByNaturalId(server.getId()).orElseThrow());
        ping.setService(serviceRepository.findByNaturalId(service.getId()).orElseThrow());
        ping.setInterval((int) interval.toMillis());
        pingRepository.saveAndFlush(ping);
    }

}
