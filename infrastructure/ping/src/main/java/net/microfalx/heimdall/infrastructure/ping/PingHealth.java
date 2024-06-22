package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Health;
import net.microfalx.heimdall.infrastructure.api.Ping;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A class which monitors the outcome of pings and calculates the health of services.
 */
@Component
public class PingHealth {

    @Autowired
    private PingProperties properties;

    /**
     * Registers the outcome of a ping.
     *
     * @param ping the ping
     */
    void registerPing(Ping ping) {

    }

    /**
     * Retries the status of the last ping.
     *
     * @param service the service
     * @param server  the server
     * @return a non-null status
     */
    Status getStatus(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        return Status.NA;
    }

    Health getHealth(net.microfalx.heimdall.infrastructure.api.Service service, Server server) {
        return Health.NA;
    }
}
