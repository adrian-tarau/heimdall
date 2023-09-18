package net.microfalx.heimdall.protocol.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
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
import java.io.StringWriter;
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
public abstract class ProtocolService<E extends Event> implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolService.class);

    private static final DateTimeFormatter DIRECTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String FILE_NAME_FORMAT = "%09d";

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
        Resource directory = resourceService.getShared(DIRECTORY_DATE_FORMATTER.format(LocalDateTime.now()));
        Resource target = directory.resolve(String.format(FILE_NAME_FORMAT, getNextSequence()));
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
        updateDocument(event, document);
        indexService.index(document);
    }

    /**
     * Accepts a protocol event, process it and stores it in the available data stores.
     *
     * @param event the event
     */
    public final void accept(E event) {
        uploadParts(event);
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
     * Encodes the attributes into a JSON.
     *
     * @param event the event
     * @return the JSON
     */
    protected final String encodeAttributes(E event) {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            objectMapper.writeValue(writer, event.getAttributes());
        } catch (IOException e) {
            // It will never happen
        }
        return writer.toString();
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

    private void uploadParts(E event) {
        for (Part part : event.getParts()) {
            if (part instanceof AbstractPart apart) {
                apart.resource = upload(apart.resource);
            }
        }
    }

    private void initializeExecutor() {
        taskExecutor = new TaskExecutorFactory().setSuffix(getEventType().name().toLowerCase()).setQueueCapacity(5000).createExecutor();
    }

    private void initializeScheduler() {
        taskScheduler = new TaskExecutorFactory().setSuffix(getEventType().name().toLowerCase()).createScheduler();
    }

    private int getNextSequence() {
        return resourceSequences.computeIfAbsent(LocalDate.now(), localDate -> new AtomicInteger(1)).getAndIncrement();
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
