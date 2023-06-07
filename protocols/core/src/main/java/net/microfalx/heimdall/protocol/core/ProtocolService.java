package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.heimdall.protocol.core.jpa.AddressRepository;
import net.microfalx.resource.NullResource;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Base class for all protocol services.
 */
public abstract class ProtocolService<E extends Event> {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private AddressRepository addressRepository;

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
     * Indexes an event.
     *
     * @param event the event to index
     * @return the document after it is indexed
     */
    @Async
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
            net.microfalx.heimdall.protocol.core.jpa.Address fromAddress = new net.microfalx.heimdall.protocol.core.jpa.Address();
            fromAddress.setType(net.microfalx.heimdall.protocol.core.jpa.Address.Type.EMAIL);
            fromAddress.setName(address.getName());
            fromAddress.setValue(address.getValue());
            addressRepository.save(fromAddress);
        }
        return addressJpa;
    }
}
