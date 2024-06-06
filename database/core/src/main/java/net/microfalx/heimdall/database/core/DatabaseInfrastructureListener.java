package net.microfalx.heimdall.database.core;

import net.microfalx.heimdall.infrastructure.api.InfrastructureListener;
import net.microfalx.lang.annotation.Provider;

@Provider
public class DatabaseInfrastructureListener implements InfrastructureListener {

    @Override
    public void onInitialization() {
        InfrastructureListener.super.onInitialization();
    }
}
