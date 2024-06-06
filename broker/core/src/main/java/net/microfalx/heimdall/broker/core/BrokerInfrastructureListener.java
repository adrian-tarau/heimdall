package net.microfalx.heimdall.broker.core;

import net.microfalx.heimdall.infrastructure.api.InfrastructureListener;
import net.microfalx.lang.annotation.Provider;

@Provider
public class BrokerInfrastructureListener implements InfrastructureListener {

    @Override
    public void onInitialization() {
        InfrastructureListener.super.onInitialization();
    }
}
