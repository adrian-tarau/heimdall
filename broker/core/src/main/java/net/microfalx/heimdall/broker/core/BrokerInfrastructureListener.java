package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.heimdall.infrastructure.api.InfrastructureListener;
import net.microfalx.lang.annotation.Provider;

@Provider
public class BrokerInfrastructureListener extends ApplicationContextSupport implements InfrastructureListener {

    @Override
    public void onInfrastructureInitialization() {
    }
}
