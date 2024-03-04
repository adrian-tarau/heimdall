package net.microfalx.heimdall.broker.kafka;

import net.microfalx.heimdall.broker.core.Broker;
import net.microfalx.heimdall.broker.core.BrokerConsumer;
import net.microfalx.heimdall.broker.core.BrokerProducer;
import net.microfalx.heimdall.broker.core.BrokerProvider;
import net.microfalx.lang.annotation.Provider;

import java.net.URI;

@Provider
public class KafkaBrokerProvider implements BrokerProvider {

    @Override
    public BrokerConsumer createConsumer(Broker broker, URI uri) {
        return new KafkaBrokerConsumer(broker, uri);
    }

    @Override
    public BrokerProducer createProducer(Broker broker, URI uri) {
        return new KafkaBrokerProducer(broker, uri);
    }

    @Override
    public boolean supports(Broker broker) {
        return broker.getType() == Broker.Type.KAFKA;
    }
}
