package net.microfalx.heimdall.broker.core;

import java.net.URI;

/**
 * An interface which can create consumers and producers.
 */
public interface BrokerProvider {

    /**
     * Creates a consumer
     *
     * @param uri the URI
     * @return a non-null instance
     */
    BrokerConsumer createConsumer(Broker broker, URI uri);

    /**
     * Creates a producer.
     *
     * @param uri the URI
     * @return a non-null instance
     */
    BrokerProducer createProducer(Broker broker, URI uri);

    /**
     * Returns whether the broker is supported by this provider.
     *
     * @param broker the broker
     * @return {@code true} if broker is supported, {@code false} otherwise
     */
    boolean supports(Broker broker);
}
