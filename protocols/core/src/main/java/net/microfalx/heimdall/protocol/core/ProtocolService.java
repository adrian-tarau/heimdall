package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchService;
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
    public Resource upload(Part part) {
        return NullResource.createNull();
    }

    /**
     * Indexes an event.
     *
     * @param event the event to index
     * @return the document after it is indexed
     */
    @Async
    public Future<Document> index(E event) {
        return CompletableFuture.completedFuture(new Document());
    }
}
