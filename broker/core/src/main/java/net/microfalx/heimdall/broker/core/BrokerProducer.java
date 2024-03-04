package net.microfalx.heimdall.broker.core;

import net.microfalx.lang.Initializable;
import net.microfalx.lang.Releasable;

import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all consumers.
 */
public abstract class BrokerProducer implements Initializable, Releasable {

    private final Broker broker;
    private final URI uri;

    public BrokerProducer(Broker broker, URI uri) {
        requireNonNull(broker);
        requireNonNull(uri);
        this.broker = broker;
        this.uri = uri;
    }
}
