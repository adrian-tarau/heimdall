package net.microfalx.heimdall.protocol.core;

import inet.ipaddr.IPAddressString;
import net.microfalx.bootstrap.core.async.ThreadPoolFactory;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import net.microfalx.resource.rocksdb.RocksDbResource;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.microfalx.bootstrap.search.Document.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.*;

/**
 * Base class for all protocol services.
 */
public abstract class ProtocolService<E extends Event, M extends net.microfalx.heimdall.protocol.core.jpa.Event> implements InitializingBean {

    private final Logger LOGGER;

    private static final DateTimeFormatter DIRECTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long STATS_INTERVAL = 300_000;
    private static final String REFERENCE_ATTR = "reference";
    private static final String FILE_NAME_FORMAT = "%09d";
    private static final String PART_FILE_EXTENSION = "part";
    public static final String PROTOCOL = "protocol";

    private static final LocalDateTime STARTUP = LocalDateTime.now();
    private final Map<String, net.microfalx.heimdall.protocol.core.jpa.Address> addressCache = new ConcurrentHashMap<>();
    private volatile Metrics metrics;

    public ProtocolService() {
        LOGGER = LoggerFactory.getLogger(getClass());
    }

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private ProtocolSimulatorProperties simulatorProperties;

    @Autowired
    private ProtocolProperties properties;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private ThreadPool threadPool;

    private Resource partsResource;
    private final Map<LocalDate, AtomicInteger> resourceSequences = new ConcurrentHashMap<>();
    private BlockingQueue<E> queue;


    /**
     * Returns the resource service.
     *
     * @return a non-null instance
     */
    public final ResourceService getResourceService() {
        return resourceService;
    }

    /**
     * Returns the index service.
     *
     * @return a non-null instance
     */
    public final IndexService getIndexService() {
        return indexService;
    }

    /**
     * Returns the search service.
     *
     * @return a non-null instance
     */
    public final SearchService getSearchService() {
        return searchService;
    }

    /**
     * Uploads the resource associated with a part in the shared data repository.
     * <p>
     * The method uploads the content to the repository, and it will return a resource to access the content.
     * <p>
     * The client can use the {@link Resource#toURI() resource URI} to store a pointer to the resource.
     *
     * @param resource the resource
     */
    public final Resource upload(Resource resource) {
        requireNonNull(resource);
        Resource directory = partsResource.resolve(DIRECTORY_DATE_FORMATTER.format(LocalDateTime.now()), Resource.Type.DIRECTORY);
        Resource target = directory.resolve(String.format(FILE_NAME_FORMAT, getNextSequence()) + "." + PART_FILE_EXTENSION);
        try {
            if (!directory.exists()) directory.create();
            target.copyFrom(resource);
        } catch (Exception e) {
            LOGGER.error("Failed to copy resource part to storage: " + target.toURI() + ", retry later", e);
        }
        return target;
    }

    /**
     * Returns the shared executor.
     *
     * @return a non-null instance
     */
    public final ThreadPool getThreadPool() {
        if (threadPool == null) initializeThreadPool();
        return threadPool;
    }

    /**
     * Indexes an event.
     *
     * @param event the event to index
     */
    public final void index(E event) {
        index(Collections.singleton(event));
    }

    /**
     * Indexes a collection of events.
     *
     * @param events the events to index
     */
    public final void index(Collection<E> events) {
        Collection<Document> documents = new ArrayList<>();
        for (E event : events) {
            if (event.getTargets().isEmpty()) {
                documents.add(index(event, null));
            } else {
                for (Address target : event.getTargets()) {
                    documents.add(index(event, target));
                }
            }
        }
        try {
            indexService.index(documents);
        } catch (Exception e) {
            LOGGER.error("Failed to index events " + events.size(), e);
        }

    }

    /**
     * Returns a list of attributes associated with the model.
     * <p>
     * The attributes will be displayed in the result set dashboards, for each model.
     *
     * @param model the model
     * @return a non-null instance
     */
    public final Attributes<?> getAttributes(M model) {
        requireNonNull(model);
        Resource resource = getAttributesResource(model);
        return readAttributes(model, resource);
    }

    /**
     * Accepts a protocol event, process it and stores it in the available data stores.
     *
     * @param event the event
     */
    public final void accept(E event) {
        for (; ; ) {
            if (queue.offer(event)) break;
            flush();
        }
    }

    /**
     * Returns the controller path used to create references.
     *
     * @return a non-null instance
     */
    protected abstract String getControllerPath();

    /**
     * Returns the event type supported by this service.
     *
     * @return a non-null instance
     */
    protected abstract Event.Type getEventType();

    /**
     * Prepares an event to be stored in the data store.
     *
     * @param event the event
     */
    protected abstract void prepare(E event);

    /**
     * Persists an event in the data store.
     *
     * @param event the event
     */
    protected abstract void persist(E event);

    /**
     * Extracts additional attributes from an event and updates the document (stored in the search engine).
     *
     * @param event    the event
     * @param document the document
     */
    protected void updateDocument(E event, Document document) {
        // empty on purpose
    }

    /**
     * Returns the simulator.
     *
     * @return the simulator, null if not supported
     */
    protected <C extends ProtocolClient<E>> ProtocolSimulator<E, C> getSimulator() {
        return null;
    }

    /**
     * Returns the resource which stores the attribute for a model.
     *
     * @param model the model
     * @return a non-null instance
     */
    protected Resource getAttributesResource(M model) {
        throw new ProtocolException("Not supported");
    }

    /**
     * Updates the event with an attribute which keeps a reference to the event to be displayed in UI.
     *
     * @param event the event
     * @param id    the event identifier
     */
    protected final void updateReference(E event, Object id) {
        requireNonNull(event);
        requireNonNull(id);
        event.add(REFERENCE_ATTR, getReference(ObjectUtils.toString(id)));
    }

    /**
     * Returns a reference to an event using a controller path.
     *
     * @param id the event identifier
     * @return the path
     */
    protected final String getReference(String id) {
        requireNotEmpty(id);
        return getControllerPath() + "#/view/" + id;
    }

    /**
     * Returns a list of attributes associated with a model from a resource (usually encoded as a JSON document).
     *
     * @param model the model
     * @return a collection of attributes
     */
    protected final Attributes<?> readAttributes(M model, Resource resource) {
        Attributes<?> attributes = Attributes.create();
        try {
            attributes.copyFrom(resource);
        } catch (Exception e) {
            String resourceFragment = NA_STRING;
            try {
                resourceFragment = StringUtils.getMaximumLines(resource.loadAsString(), 5);
            } catch (Exception ex) {
                // ignore
            }
            LOGGER.error("Failed to load attributes from " + resource.toURI() + ", content: " + resourceFragment, e);
        }
        return attributes;
    }

    /**
     * Looks up an address in the database based on the value.
     * <p>
     * If the address does not exist, create a new entry
     *
     * @param address the address
     * @return the JPA address
     */
    protected final net.microfalx.heimdall.protocol.core.jpa.Address lookupAddress(Address address) {
        requireNonNull(address);
        String addressValue = address.getValue();
        net.microfalx.heimdall.protocol.core.jpa.Address cachedAddress = addressCache.get(addressValue);
        if (cachedAddress != null) return cachedAddress;
        RetryTemplate template = new RetryTemplate();
        return template.execute(context -> {
            net.microfalx.heimdall.protocol.core.jpa.Address addressJpa = addressRepository.findByValue(addressValue);
            if (addressJpa == null) {
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                addressJpa = transactionTemplate.execute(status -> {
                    net.microfalx.heimdall.protocol.core.jpa.Address newAddressJpa = new net.microfalx.heimdall.protocol.core.jpa.Address();
                    newAddressJpa.setType(address.getType());
                    newAddressJpa.setName(resolveName(address));
                    newAddressJpa.setValue(addressValue);
                    addressRepository.save(newAddressJpa);
                    return newAddressJpa;
                });
                addressCache.put(addressValue, addressJpa);
            }
            return addressJpa;
        });
    }

    /**
     * Times a protocol operation.
     *
     * @param name     the name
     * @param supplier the supplier
     * @param <T>      the return type
     * @return the return value
     */
    protected final <T> T time(String name, Supplier<T> supplier) {
        Metrics metrics = getMetrics();
        return metrics.time(name, supplier);
    }

    /**
     * Times a protocol operation.
     *
     * @param name     the name
     * @param consumer the consumer
     * @param <T>      the return type
     */
    protected final <T> void time(String name, Consumer<T> consumer) {
        Metrics metrics = getMetrics();
        metrics.time(name, consumer, null);
    }

    /**
     * Counts a protocol operation.
     *
     * @param name the name
     */
    protected final void count(String name) {
        Metrics metrics = getMetrics();
        metrics.count(name);
    }

    /**
     * Returns the metrics associated with this protocol.
     *
     * @return a non-null instance
     */
    protected final Metrics getMetrics() {
        if (metrics == null) metrics = Metrics.of("Protocol").withGroup(getEventType().name());
        return metrics;
    }

    /**
     * Persists the event part in the database.
     *
     * @param part the part
     * @return the JPA part
     */
    protected final net.microfalx.heimdall.protocol.core.jpa.Part persistPart(Part part) {
        return time("Persist Part", () -> {
            net.microfalx.heimdall.protocol.core.jpa.Part partJpa = new net.microfalx.heimdall.protocol.core.jpa.Part();
            partJpa.setType(part.getType());
            if (!NA_STRING.equals(part.getName())) {
                partJpa.setName(org.apache.commons.lang3.StringUtils.abbreviate(part.getName(), 90));
            }
            partJpa.setFileName(part.getFileName());
            if (part instanceof AbstractPart apart) {
                apart.resource = upload(apart.resource);
            }
            try {
                partJpa.setLength((int) part.getResource().length());
            } catch (IOException e) {
                partJpa.setLength(-1);
            }
            partJpa.setMimeType(MimeType.get(part.getMimeType()));
            partJpa.setResource(part.getResource().toURI().toASCIIString());
            partJpa.setCreatedAt(part.getEvent().getCreatedAt().toLocalDateTime());
            partRepository.save(partJpa);
            return partJpa;
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeThreadPool();
        initializeSimulator();
        initializeQueue();
        initResources();
    }

    private void initializeSimulator() {
        if (!simulatorProperties.isEnabled()) return;
        LOGGER.info("Simulator is enabled, interval {}", simulatorProperties.getInterval());
        getThreadPool().scheduleAtFixedRate(new SimulatorWorker(), Duration.ZERO, simulatorProperties.getInterval());
    }

    private void initResources() {
        partsResource = resourceService.getShared("parts");
        if (partsResource.isLocal()) {
            LOGGER.info("Protocol parts are stored in a RocksDB database: {}", partsResource);
            Resource dbPartsResource = RocksDbResource.create(partsResource);
            try {
                dbPartsResource.create();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize parts store", e);
                System.exit(10);
            }
            ResourceFactory.registerSymlink("parts", dbPartsResource);
        } else {
            LOGGER.info("Protocol parts are stored in a remote storage: {}", partsResource);
        }
    }

    private void initializeQueue() {
        queue = new ArrayBlockingQueue<>(properties.getBatchSize());
        getThreadPool().scheduleAtFixedRate(new FlushWorker(), Duration.ZERO, properties.getBatchInterval());
    }

    private void initializeThreadPool() {
        if (threadPool == null) {
            threadPool = ThreadPoolFactory.create(capitalizeFirst(getEventType().name()))
                    .setQueueCapacity(5000).create();
        }
    }

    private int getNextSequence() {
        return resourceSequences.computeIfAbsent(LocalDate.now(), localDate -> {
            int start = 1;
            if (STARTUP.toLocalDate().equals(localDate)) {
                start = STARTUP.toLocalTime().toSecondOfDay();
            }
            return new AtomicInteger(start);
        }).getAndIncrement();
    }

    private String resolveName(Address address) {
        if (address.getType() != Address.Type.HOSTNAME) return address.getName();
        if (!new IPAddressString(address.getName()).isIPAddress()) return address.getName();
        try {
            InetAddress inetAddress = InetAddress.getByName(address.getValue());
            return inetAddress.getCanonicalHostName();
        } catch (Exception e) {
            return address.getName();
        }
    }

    private Document index(E event, Address target) {
        return time("Index", () -> {
            Document document = create(event.getId(), event.getName());
            document.setType(event.getType().name().toLowerCase());
            document.setCreatedAt(event.getCreatedAt());
            document.setSentAt(event.getSentAt());
            document.setReceivedAt(event.getReceivedAt());
            if (event.getBody() != null) document.setBody(event.getBody().getResource());
            net.microfalx.heimdall.protocol.core.jpa.Address sourceJpa = lookupAddress(event.getSource());
            document.add(Attribute.create(SOURCE_FIELD, sourceJpa.getName()).enableAll());
            if (target != null) {
                net.microfalx.heimdall.protocol.core.jpa.Address targetJpa = lookupAddress(target);
                document.add(Attribute.create(TARGET_FIELD, targetJpa.getName()).enableAll());
            }
            document.add(Attribute.create(SEVERITY_FIELD, capitalizeWords(event.getSeverity().name())).enableAll());
            net.microfalx.bootstrap.model.Attribute reference = event.get(REFERENCE_ATTR);
            event.remove(REFERENCE_ATTR);
            for (net.microfalx.bootstrap.model.Attribute attribute : event) {
                document.addIfAbsent(Attribute.create(attribute).enableAll());
            }
            if (reference != null) {
                document.setReference(reference.asString());
            }
            document.setOwner(PROTOCOL);
            updateDocument(event, document);
            return document;
        });
    }

    private void flush() {
        Collection<E> events = new ArrayList<>();
        queue.drainTo(events);
        if (!events.isEmpty()) {
            prepare(events);
            persist(events);
            index(events);
        }
    }

    private void prepare(Collection<E> events) {
        for (E event : events) {
            count("Events");
            try {
                prepare(event);
            } catch (Exception e) {
                LOGGER.error("Failed to prepare event " + event, e);
            }
        }
    }

    private void persist(Collection<E> events) {
        AtomicBoolean persisted = new AtomicBoolean(true);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            for (E event : events) {
                try {
                    time("Persist", (t) -> persist(event));
                } catch (Exception e) {
                    status.setRollbackOnly();
                    persisted.set(false);
                    LOGGER.warn("Failed to persist event in a common transaction, rollback and persist " +
                            "individual events, current event: " + event, e);
                }
            }
            return null;
        });
        if (!persisted.get()) {
            persistSingleTransactions(events);
        }
    }

    private void persistSingleTransactions(Collection<E> events) {
        for (E event : events) {
            try {
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.execute(status -> {
                    time("Persist", (t) -> persist(event));
                    return null;
                });
            } catch (Exception e) {
                LOGGER.error("Failed to persist event: " + event, e);
            }
        }
    }

    class SimulatorWorker implements Runnable {

        @Override
        public void run() {
            ProtocolSimulator<E, ?> simulator = getSimulator();
            if (simulator != null) {
                simulator.simulate();
            }
        }
    }

    class FlushWorker implements Runnable {

        @Override
        public void run() {
            flush();
        }
    }
}
