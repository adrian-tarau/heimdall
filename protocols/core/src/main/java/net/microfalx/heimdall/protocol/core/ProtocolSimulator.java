package net.microfalx.heimdall.protocol.core;

import net.datafaker.Faker;
import net.datafaker.providers.base.Shakespeare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all simulators.
 */
public abstract class ProtocolSimulator<E extends Event, C extends ProtocolClient<E>> implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolService.class);

    private final ProtocolSimulatorProperties properties;

    protected final Random random = ThreadLocalRandom.current();
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

    /**
     * Returns an random enum.
     *
     * @param enumClass the enum class
     * @param <ENUM>    the enum type
     * @return the enum value
     */
    @SuppressWarnings("unchecked")
    protected final <ENUM extends Enum<ENUM>> ENUM getNextEnum(Class<ENUM> enumClass) {
        return enumClass.getEnumConstants()[random.nextInt(enumClass.getEnumConstants().length)];
    }

    protected final String getNextName() {
        return getNextSentence();
    }

    protected final String getNextBody() {
        StringBuilder builder = new StringBuilder();
        int paragraphCount = 3 + random.nextInt(20);
        for (int i = 0; i < paragraphCount; i++) {
            int sentenceCount = 1 + random.nextInt(5);
            for (int j = 0; j < sentenceCount; j++) {
                builder.append(getNextSentence());
            }
            if (i < (paragraphCount - 1)) builder.append("\n");
        }
        return builder.toString();
    }

    private String getNextSentence() {
        Faker faker = new Faker();
        Shakespeare shakespeare = faker.shakespeare();
        float value = random.nextFloat();
        if (value > 0.9) {
            return faker.greekPhilosopher().quote();
        } else if (value > 0.8) {
            return faker.gameOfThrones().quote();
        } else if (value > 0.7) {
            return faker.freshPrinceOfBelAir().quotes();
        } else if (value > 0.6) {
            return shakespeare.asYouLikeItQuote();
        } else if (value > 0.5) {
            return shakespeare.hamletQuote();
        } else if (value > 0.4) {
            return shakespeare.kingRichardIIIQuote();
        } else if (value > 0.3) {
            return shakespeare.kingRichardIIIQuote();
        } else if (value > 0.2) {
            return faker.heyArnold().quotes();
        } else {
            return faker.futurama().quote();
        }
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
