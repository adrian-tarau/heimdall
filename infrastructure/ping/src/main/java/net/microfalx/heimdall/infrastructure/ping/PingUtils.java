package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;

public class PingUtils {

    /**
     * Returns a natural id for a service & server tuple.
     *
     * @param service the service
     * @param server  the server
     * @return a non-emtpy String
     */
    public static String getId(Service service, Server server) {
        return service.getId() + "_" + server.getId();
    }
}
