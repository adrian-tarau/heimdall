package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Ping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PingPersistence {

    @Autowired
    private PingRepository pingRepository;

    @Autowired
    private PingResultRepository pingResultRepository;

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

}
