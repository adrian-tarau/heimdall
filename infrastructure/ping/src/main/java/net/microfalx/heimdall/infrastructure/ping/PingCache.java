package net.microfalx.heimdall.infrastructure.ping;

import net.microfalx.heimdall.infrastructure.api.Cluster;
import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureListener;
import net.microfalx.heimdall.infrastructure.api.Service;
import org.springframework.stereotype.Component;

/**
 * Loads ping related data from the database.
 */
@Component
class PingCache implements InfrastructureListener {

    /**
     * Invoked when there is a need to reload the cache from database.
     */
    void reload() {

    }

    @Override
    public void onEnvironmentChanged(Environment environment) {
        reload();
    }

    @Override
    public void onClusterChanged(Cluster cluster) {
        reload();
    }

    @Override
    public void onServiceChanged(Service service) {
        reload();
    }
}
