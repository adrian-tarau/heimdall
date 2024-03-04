package net.microfalx.heimdall.broker.kafka;

import net.microfalx.heimdall.broker.core.Broker;
import net.microfalx.heimdall.broker.core.BrokerProducer;

import java.net.URI;

public class KafkaBrokerProducer extends BrokerProducer {

    public KafkaBrokerProducer(Broker broker, URI uri) {
        super(broker, uri);
    }

    @Override
    public void initialize(Object... context) {

    }

    @Override
    public void release() {

    }
}
