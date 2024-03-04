package net.microfalx.heimdall.broker.kafka;

import net.microfalx.heimdall.broker.core.Broker;
import net.microfalx.heimdall.broker.core.BrokerConsumer;

import java.net.URI;

public class KafkaBrokerConsumer extends BrokerConsumer {

    public KafkaBrokerConsumer(Broker broker, URI uri) {
        super(broker, uri);
    }

    @Override
    public void initialize(Object... context) {

    }

    @Override
    public void release() {

    }
}
