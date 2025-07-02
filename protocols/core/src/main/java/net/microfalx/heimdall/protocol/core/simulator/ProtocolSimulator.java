package net.microfalx.heimdall.protocol.core.simulator;

import net.datafaker.Faker;
import net.datafaker.providers.base.Shakespeare;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.core.Event;
import net.microfalx.heimdall.protocol.core.ProtocolClient;
import net.microfalx.lang.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseMessage;

/**
 * Base class for all simulators.
 */
public abstract class ProtocolSimulator<E extends Event, C extends ProtocolClient<E>> implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolSimulator.class);

    private final ProtocolSimulatorProperties properties;

    protected final Random random = ThreadLocalRandom.current();
    private final AtomicInteger SUBNET_INDEX1_GENERATOR = new AtomicInteger(1);
    private final AtomicInteger SUBNET_INDEX2_GENERATOR = new AtomicInteger(1);
    private final List<Address> sourceAddresses = new ArrayList<>();
    private final List<Address> targetAddresses = new ArrayList<>();
    private final List<ProtocolClient<E>> clients = new ArrayList<>();
    private final Faker faker = new Faker();
    private final Lock lock = new ReentrantLock();

    public ProtocolSimulator(ProtocolSimulatorProperties properties) {
        requireNonNull(properties);
        this.properties = properties;
    }

    public ProtocolSimulatorProperties getProperties() {
        return properties;
    }

    /**
     * Returns the event type supported by this simulator.
     *
     * @return a non-null instance
     */
    protected abstract Event.Type getEventType();

    /**
     * Returns whether the simulator is enabled.
     *
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /**
     * Returns whether the simulator should use external data sets.
     *
     * @return a non-null instance
     */
    public boolean shouldUseExternalDataSets() {
        return properties.isUseExternalDataSets();
    }

    /**
     * Returns the data generator associated with this simulator.
     *
     * @return a non-null instance
     */
    protected final Faker getFaker() {
        return faker;
    }

    /**
     * Invoked to simulate events.
     * <p>
     * If the simulator is not enabled, the method returns right away.
     */
    public final void simulate() {
        if (!isEnabled()) return;
        if (lock.tryLock()) {
            try {
                simulateUnderLock();
            } catch (Exception e) {
                LOGGER.atError().setCause(e).log("Failed to simulate events for {}, root cause: {}", getEventType(),
                        getRootCauseMessage(e));
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!isEnabled()) return;
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
    protected abstract Collection<C> createClients();

    /**
     * Simulates an event.
     *
     * @param client        the client
     * @param sourceAddress the source address
     * @param targetAddress the target address
     * @param index         the event index
     * @throws IOException if an I/O error occurs
     */
    protected abstract void simulate(C client, Address sourceAddress, Address targetAddress, int index) throws IOException;

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
    protected final <ENUM extends Enum<ENUM>> ENUM getRandomEnum(Class<ENUM> enumClass) {
        return enumClass.getEnumConstants()[random.nextInt(enumClass.getEnumConstants().length)];
    }

    /**
     * Returns the next random IP.
     *
     * @return a non-null instance
     */
    protected final String getRandomIp(boolean source) {
        if (source && random.nextFloat() > 0.5) {
            return PUBLIC_IPS[random.nextInt(PUBLIC_IPS.length)];
        } else {
            int iterations = random.nextInt(10);
            for (int i = 0; i < iterations; i++) {
                SUBNET_INDEX2_GENERATOR.incrementAndGet();
                if (SUBNET_INDEX1_GENERATOR.get() >= 255) {
                    SUBNET_INDEX1_GENERATOR.set(1);
                }
            }
            return "192.168." + SUBNET_INDEX2_GENERATOR.get() + "." + SUBNET_INDEX1_GENERATOR.getAndIncrement();
        }
    }

    /**
     * Returns the next domain.
     *
     * @return a non-null instance
     */
    protected final String getRandomDomain() {
        return faker.domain().fullDomain("microfalx");
    }

    /**
     * Returns the next domain or IP.
     *
     * @return a non-null instance
     */
    protected final String getRandomDomainOrIp(boolean source) {
        if (random.nextFloat() > 0.8) {
            return getRandomIp(source);
        } else {
            return getRandomDomain();
        }
    }

    /**
     * Returns the next event name/title/caption.
     *
     * @return a non-null instance
     */
    protected final String getRandomSentence() {
        return getNextSentence();
    }

    /**
     * Returns the next name.
     *
     * @return a non-null instance
     */
    protected final String getRandomName() {
        float value = random.nextFloat();
        if (value > 0.9) {
            return faker.appliance().equipment();
        } else if (value > 0.8) {
            return faker.brand().car();
        } else if (value > 0.7) {
            return faker.brand().watch();
        } else if (value > 0.6) {
            return faker.commerce().productName();
        } else {
            return faker.construction().materials();
        }
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
     * @param min the minimum number of bytes
     * @param min the maximum number of bytes
     * @return a non-null instance
     */
    protected final byte[] getRandomBytes(int min, int max) {
        return getRandomBytes(min, max, false);
    }

    /**
     * Returns the next body of random data.
     *
     * @param min        the minimum number of bytes
     * @param min        the maximum number of bytes
     * @param compresses {@code true} to compress data,  {@code false} otherwise
     * @return a non-null instance
     */
    protected final byte[] getRandomBytes(int min, int max, boolean compresses) {
        byte[] data = new byte[min + random.nextInt(Math.abs(max - min))];
        random.nextBytes(data);
        if (compresses) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                OutputStream outputStream = new GZIPOutputStream(buffer);
                outputStream.write(data);
                outputStream.close();
                data = buffer.toByteArray();
            } catch (IOException e) {
                return data;
            }
        }
        return data;
    }

    private void simulateUnderLock() {
        int eventCount = properties.getMinimumEventCount() + ThreadLocalRandom.current().nextInt(properties.getMaximumEventCount());
        for (int i = 0; i < eventCount; i++) {
            Address sourceAddress = getRandomSourceAddress();
            Address targetAddress = getRandomTargetAddress();
            C client = (C) getRandomClient();
            try {
                simulate(client, sourceAddress, targetAddress, i + 1);
            } catch (IOException e) {
                LOGGER.atWarn().setCause(e).log("Failed to send simulated event to {}", client.getHostName());
            }
            waitForRate();
        }
    }

    private void waitForRate() {
        ThreadUtils.sleepMillis(1000 / properties.getRate());
    }

    private String getNextSentence() {
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
        int addressCount = properties.getMinimumAddressCount() + random.nextInt(properties.getMaximumAddressCount());
        for (int i = 0; i < addressCount; i++) {
            this.targetAddresses.add(createTargetAddress());
        }
        addressCount = properties.getMinimumAddressCount() + random.nextInt(properties.getMaximumAddressCount());
        for (int i = 0; i < addressCount; i++) {
            this.sourceAddresses.add(createSourceAddress());
        }
    }

    private void initializeClient() {
        clients.addAll(createClients());
    }

    private static final String[] PUBLIC_IPS = new String[]{
            "142.251.32.110", "142.251.40.165", "142.251.40.238",
            "20.236.44.162", "20.112.250.133", "20.231.239.246", "31.13.71.36",
            "54.237.226.164", "52.3.144.142", "52.94.236.248", "54.239.28.85"
    };


}
