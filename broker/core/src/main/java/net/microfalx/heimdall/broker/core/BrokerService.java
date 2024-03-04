package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.jdbc.support.DatabaseService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A service which manages a collection of brokers.
 */
@Service
public class BrokerService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private final Collection<BrokerProvider> providers = new CopyOnWriteArrayList<>();
    private final Map<String, Broker> brokers = new ConcurrentHashMap<>();

    /**
     * Registers a broker.
     *
     * @param broker the broker
     */
    public void registerBroker(Broker broker) {
        requireNonNull(broker);
        brokers.put(toIdentifier(broker.getId()), broker);
    }

    /**
     * Returns registered brokers.
     *
     * @return a non-null instance
     */
    public Collection<Broker> getBrokers() {
        return unmodifiableCollection(brokers.values());
    }

    /**
     * Returns the broker by its identifier.
     *
     * @param id the broker identifier
     * @return the broker
     * @throws BrokerNotFoundException if such a broker is not registered
     */
    public Broker getBroker(String id) {
        requireNotEmpty(id);
        Broker broker = brokers.get(toIdentifier(id));
        if (broker == null) {
            throw new BrokerNotFoundException("A broker with identifier '" + id + "' is not registered");
        }
        return broker;
    }

    /**
     * Returns the broker by a topic URI.
     *
     * @param uri the topic URI
     * @return the broker
     * @throws BrokerNotFoundException if such a broker is not registered
     */
    public Broker getBroker(URI uri) {
        requireNonNull(uri);
        String host = uri.getHost();
        if (StringUtils.isEmpty(host)) {
            throw new BrokerNotFoundException("A hostname is required to locate a broker, URI '" + uri + "'");
        }
        return getBroker(host);
    }

    /**
     * Creates a consumer
     *
     * @param uri the URI
     * @return a non-null instance
     */
    public BrokerConsumer createConsumer(URI uri) {
        requireNonNull(uri);
        Broker broker = getBroker(uri);
        BrokerProvider brokerProvider = locateProvider(uri);
        return brokerProvider.createConsumer(broker, uri);
    }

    /**
     * Creates a producer.
     *
     * @param uri the URI
     * @return a non-null instance
     */
    public BrokerProducer createProducer(URI uri) {
        requireNonNull(uri);
        Broker broker = getBroker(uri);
        BrokerProvider brokerProvider = locateProvider(uri);
        return brokerProvider.createProducer(broker, uri);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadProviders();
    }

    private BrokerProvider locateProvider(URI uri) {
        Broker broker = getBroker(uri);
        for (BrokerProvider provider : providers) {
            if (provider.supports(broker)) return provider;
        }
        throw new BrokerException("A provider to support topic '" + uri + "' could not be located");
    }

    private void loadProviders() {
        LOGGER.info("Load providers:");
        Collection<BrokerProvider> loadedProviders = ClassUtils.resolveProviderInstances(BrokerProvider.class);
        for (BrokerProvider loadedProvider : loadedProviders) {
            LOGGER.info(" - " + ClassUtils.getName(loadedProvider));
            providers.add(loadedProvider);
        }
    }
}
