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
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all simulators.
 */
public abstract class ProtocolSimulator<E extends Event, C extends ProtocolClient<E>> implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolService.class);

    private final ProtocolSimulatorProperties properties;

    protected final Random random = ThreadLocalRandom.current();
    private final AtomicInteger SUBNET_INDEX1_GENERATOR = new AtomicInteger(1);
    private final AtomicInteger SUBNET_INDEX2_GENERATOR = new AtomicInteger(1);
    private final List<Address> sourceAddresses = new ArrayList<>();
    private final List<Address> targetAddresses = new ArrayList<>();
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
            Address sourceAddress = getRandomSourceAddress();
            Address targetAddress = getRandomTargetAddress();
            ProtocolClient<E> client = getRandomClient();
            try {
                simulate(client, sourceAddress, targetAddress, i + 1);
            } catch (IOException e) {
                LOGGER.warn("Failed to send simulated event to " + client.getHostName(), e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeAddresses();
        initializeClient();
        initializeData();
    }

    /**
     * Invoked to create a list of target addresses.
     *
     * @return an address
     */
    protected abstract Address createSourceAddress();

    /**
     * Invoked to create a list of source addresses.
     *
     * @return an address
     */
    protected abstract Address createTargetAddress();

    /**
     * Invoked to create the client.
     *
     * @return a non-null instance
     */
    protected abstract Collection<ProtocolClient<E>> createClients();

    /**
     * Simulates an event.
     *
     * @param client        the client
     * @param sourceAddress the source address
     * @param targetAddress the target address
     * @param index         the event index
     * @throws IOException if an I/O error occurs
     */
    protected abstract void simulate(ProtocolClient<E> client, Address sourceAddress, Address targetAddress, int index) throws IOException;

    /**
     * Registers data for simulator.
     *
     * @throws IOException if an I/O failure occurs
     */
    protected void initializeData() throws IOException {
        // empty on purpose
    }

    /**
     * Returns the next target address to simulate an event.
     *
     * @return a non-null instance
     */
    protected final Address getRandomTargetAddress() {
        return targetAddresses.get(ThreadLocalRandom.current().nextInt(targetAddresses.size()));
    }

    /**
     * Returns the next source address to simulate an event.
     *
     * @return a non-null instance
     */
    protected final Address getRandomSourceAddress() {
        return sourceAddresses.get(ThreadLocalRandom.current().nextInt(sourceAddresses.size()));
    }

    /**
     * Returns the next target address to simulate an event.
     *
     * @return a non-null instance
     */
    protected final ProtocolClient<E> getRandomClient() {
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
    protected final <ENUM extends Enum<ENUM>> ENUM getRandomEnum(Class<ENUM> enumClass) {
        return enumClass.getEnumConstants()[random.nextInt(enumClass.getEnumConstants().length)];
    }

    /**
     * Returns the next subnet.
     *
     * @return a non-null instance
     */
    protected final String getRandomSubnet() {
        if (SUBNET_INDEX1_GENERATOR.get() >= 255) {
            SUBNET_INDEX1_GENERATOR.set(1);
            SUBNET_INDEX2_GENERATOR.incrementAndGet();
        }
        return SUBNET_INDEX2_GENERATOR.get() + "." + SUBNET_INDEX1_GENERATOR.getAndIncrement();
    }

    /**
     * Returns the next event name/title/caption.
     *
     * @return a non-null instance
     */
    protected final String getRandomName() {
        return getNextSentence();
    }

    /**
     * Returns the next body of text.
     *
     * @return a non-null instance
     */
    protected final String getRandomText() {
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

    /**
     * Returns the next body of random data.
     *
     * @return a non-null instance
     */
    protected final byte[] getRandomBytes(int min, int max) {
        byte[] data = new byte[min + random.nextInt(Math.abs(max - min))];
        random.nextBytes(data);
        return data;
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
            this.targetAddresses.add(createTargetAddress());
        }
        addressCount = properties.getMinimumAddressCount() + ThreadLocalRandom.current().nextInt(properties.getMaximumAddressCount());
        for (int i = 0; i < addressCount; i++) {
            this.sourceAddresses.add(createSourceAddress());
        }
    }

    private void initializeClient() {
        clients.addAll(createClients());
    }
}
