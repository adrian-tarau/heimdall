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
     * Saves the result of a ping in the database.
     *
     * @param ping the ping
     */
    void save(Ping ping) {

    }

}
