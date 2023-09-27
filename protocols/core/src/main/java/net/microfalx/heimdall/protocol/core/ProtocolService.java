package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.heimdall.protocol.core.jpa.PartRepository;
import net.microfalx.lang.IOUtils;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.NA_STRING;

/**
 * Base class for all protocol services.
 */
public abstract class ProtocolService<E extends Event, M extends net.microfalx.heimdall.protocol.core.jpa.Event> implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolService.class);

    private static final DateTimeFormatter DIRECTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String FILE_NAME_FORMAT = "%09d";
    private static final String PART_FILE_EXTENSION = "part";

    private static final LocalDateTime STARTUP = LocalDateTime.now();

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
    private TaskScheduler taskScheduler;

    private AsyncTaskExecutor taskExecutor;

    private Map<LocalDate, AtomicInteger> resourceSequences = new ConcurrentHashMap<>();

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
        Resource directory = resourceService.getShared("parts");
        directory = directory.resolve(DIRECTORY_DATE_FORMATTER.format(LocalDateTime.now()), Resource.Type.DIRECTORY);
        Resource target = directory.resolve(String.format(FILE_NAME_FORMAT, getNextSequence()) + "." + PART_FILE_EXTENSION);
        try {
            if (!directory.exists()) directory.create();
            IOUtils.appendStream(target.getOutputStream(), resource.getInputStream());
        } catch (Exception e) {
            LOGGER.error("Failed to copy resource part to storage: " + target.toURI() + ", retry later", e);
        }
        return target;
    }

    /**
     * Returns the shared scheduler.
     *
     * @return a non-null instance
     */
    public final TaskScheduler getTaskScheduler() {
        if (taskExecutor == null) initializeScheduler();
        return taskScheduler;
    }

    /**
     * Returns the shared executor.
     *
     * @return a non-null instance
     */
    public final AsyncTaskExecutor getTaskExecutor() {
        if (taskExecutor == null) initializeExecutor();
        return taskExecutor;
    }

    /**
     * Indexes an event.
     *
     * @param event the event to index
     */
    public final void index(E event) {
        if (event.getTargets().isEmpty()) {
            index(event, null);
        } else {
            for (Address target : event.getTargets()) {
                index(event, target);
            }
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
        try {
            persist(event);
        } catch (Exception e) {
            LOGGER.error("Failed to persist event" + event, e);
        }
        try {
            index(event);
        } catch (Exception e) {
            LOGGER.error("Failed to index event " + event, e);
        }
    }

    /**
     * Returns the event type supported by this service.
     *
     * @return a non-null instance
     */
    protected abstract Event.Type getEventType();

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
     * Returns a list of attributes associated with a model from a resource (usually encoded as a JSON document).
     *
     * @param model the model
     * @return a collection of attributes
     */
    protected final Attributes<?> readAttributes(M model, Resource resource) {
        Attributes<?> attributes = Attributes.create();
        try {
            attributes.copyFrom(resource);
            return attributes;
        } catch (IOException e) {
            throw new ProtocolException("Failed to load attributes from " + resource.toURI(), e);
        }
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
        net.microfalx.heimdall.protocol.core.jpa.Address addressJpa = addressRepository.findByValue(address.getValue());
        if (addressJpa == null) {
            addressJpa = new net.microfalx.heimdall.protocol.core.jpa.Address();
            addressJpa.setType(address.getType());
            addressJpa.setName(address.getName());
            addressJpa.setValue(address.getValue());
            net.microfalx.heimdall.protocol.core.jpa.Address finalAddressJpa = addressJpa;
            RetryTemplate template = new RetryTemplate();
            template.execute(context -> {
                addressRepository.save(finalAddressJpa);
                return null;
            });
        }
        return addressJpa;
    }

    /**
     * Persists the event part in the database.
     *
     * @param part the part
     * @return the JPA part
     */
    protected final net.microfalx.heimdall.protocol.core.jpa.Part persistPart(Part part) {
        net.microfalx.heimdall.protocol.core.jpa.Part partJpa = new net.microfalx.heimdall.protocol.core.jpa.Part();
        partJpa.setType(part.getType());
        if (!NA_STRING.equals(part.getName())) partJpa.setName(part.getName());
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
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeExecutor();
        initializeSimulator();
    }

    private void initializeSimulator() {
        if (!simulatorProperties.isEnabled()) return;
        LOGGER.info("Simulator is enabled, interval " + simulatorProperties.getInterval());
        getTaskScheduler().schedule(new SimulatorWorker(), new PeriodicTrigger(simulatorProperties.getInterval()));
    }

    private void initializeExecutor() {
        taskExecutor = TaskExecutorFactory.create(getEventType().name().toLowerCase()).setQueueCapacity(5000).createExecutor();
    }

    private void initializeScheduler() {
        taskScheduler = TaskExecutorFactory.create(getEventType().name().toLowerCase()).createScheduler();
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

    private void index(E event, Address target) {
        Document document = Document.create(event.getId(), event.getName());
        document.setType(event.getType().name().toLowerCase());
        document.setCreatedAt(event.getCreatedAt());
        document.setSentAt(event.getSentAt());
        document.setReceivedAt(event.getReceivedAt());
        if (event.getBody() != null) document.setBody(event.getBody().getResource());
        document.addAttribute(Attribute.create("source", event.getSource().toDisplay()).setTokenized(true));
        if (target != null) document.addAttribute(Attribute.create("target", target.toDisplay()).setTokenized(true));
        document.addAttribute(Attribute.create("severity", event.getSeverity().name()).setIndexed(true));
        for (net.microfalx.bootstrap.model.Attribute attribute : event) {
            document.addAttributeIfAbsent(Attribute.create(attribute).setIndexed(true).setStored(true));
        }
        updateDocument(event, document);
        indexService.index(document);
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
}
