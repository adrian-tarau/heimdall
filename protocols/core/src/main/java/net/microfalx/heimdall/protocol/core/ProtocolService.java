package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.resource.NullResource;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Base class for all protocol services.
 */
public abstract class ProtocolService<E extends Event> implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolService.class);

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProtocolSimulatorProperties simulatorProperties;

    @Autowired
    @Qualifier("protocol-executor")
    private ThreadPoolTaskScheduler taskExecutor;

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
     * Uploads a part in the repository associated with the protocol.
     * <p>
     * The method uploads the content to the repository and it will return a resource to access the content.
     * <p>
     * The client can use the {@link Resource#toURI() resource URI} to store a pointer to the resource.
     *
     * @param part the part
     */
    public final Resource upload(Part part) {
        return NullResource.createNull();
    }

    /**
     * Returns the shared executor.
     *
     * @return a non-null instance
     */
    public final ThreadPoolTaskScheduler getTaskScheduler() {
        return taskExecutor;
    }

    /**
     * Indexes an event.
     *
     * @param event the event to index
     * @return the document after it is indexed
     */
    public final Future<Document> index(E event) {
        Document document = Document.create(event.getId(), event.getName());
        indexService.index(document);
        return CompletableFuture.completedFuture(new Document());
    }

    /**
     * Extracts additional attributes from an event.
     *
     * @param event    the event
     * @param document the document
     */
    protected void updateDocument(E event, Document document) {

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
            addressJpa.setType(net.microfalx.heimdall.protocol.core.jpa.Address.Type.EMAIL);
            addressJpa.setName(address.getName());
            addressJpa.setValue(address.getValue());
            addressRepository.save(addressJpa);
        }
        return addressJpa;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeSimulator();
    }

    private void initializeSimulator() {
        if (!simulatorProperties.isEnabled()) return;
        LOGGER.info("Simulator is enabled, interval " + simulatorProperties.getInterval());
        getTaskScheduler().schedule(new SimulatorWorker(), new PeriodicTrigger(simulatorProperties.getInterval()));
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
