package net.microfalx.heimdall.protocol.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all simulators.
 */
public abstract class ProtocolSimulator<E extends Event, C extends ProtocolClient<E>> implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolService.class);

    private final ProtocolSimulatorProperties properties;

    private final List<Address> addresses = new ArrayList<>();
    private final List<ProtocolClient<E>> clients = new ArrayList<>();

    public ProtocolSimulator(ProtocolSimulatorProperties properties) {
        requireNonNull(properties);
        this.properties = properties;
    }

    public ProtocolSimulatorProperties getProperties() {
        return properties;
    }

    /**
     * Invoked to simulate an event, if the simulator is enabled.
     */
    public void simulate() {
        int eventCount = properties.getMinimumEventCount() + ThreadLocalRandom.current().nextInt(properties.getMaximumEventCount());
        for (int i = 0; i < eventCount; i++) {
            Address address = getNextAddress();
            ProtocolClient<E> client = getNextClient();
            try {
                simulate(client, address, i + 1);
            } catch (IOException e) {
                LOGGER.warn("Failed to send simulated event to " + client.getHostName() + ", root cause: " + e.getMessage());
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    public final void initialize() {
        initializeAddresses();
        initializeClient();
    }

    /**
     * Invoked to create a list of addresses
     *
     * @return an address
     */
    protected abstract Address createAddress();

    /**
     * Invoked to create the client.
     *
     * @return a non-null instance
     */
    protected abstract Collection<ProtocolClient<E>> createClients();

    /**
     * Simulates an event.
     *
     * @param client  the client
     * @param address the target address
     * @param index   the event index
     * @throws IOException if an I/O error occurs
     */
    protected abstract void simulate(ProtocolClient<E> client, Address address, int index) throws IOException;

    /**
     * Returns the next target address to simulate an event.
     *
     * @return a non-null instance
     */
    protected final Address getNextAddress() {
        return addresses.get(ThreadLocalRandom.current().nextInt(addresses.size()));
    }

    /**
     * Returns the next target address to simulate an event.
     *
     * @return a non-null instance
     */
    protected final ProtocolClient<E> getNextClient() {
        return clients.get(ThreadLocalRandom.current().nextInt(clients.size()));
    }


    private void initializeAddresses() {
        int addressCount = properties.getMinimumAddressCount() + ThreadLocalRandom.current().nextInt(properties.getMaximumAddressCount());
        for (int i = 0; i < addressCount; i++) {
            this.addresses.add(createAddress());
        }
    }

    private void initializeClient() {
        clients.addAll(createClients());
    }
}
