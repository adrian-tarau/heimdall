package net.microfalx.heimdall.protocol.core.simulator;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.JvmUtils;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.FormatterUtils.formatBytes;

/**
 * Base class for protocol data sets.
 *
 * @param <M>  the model type
 * @param <F>  the field type
 * @param <ID> the identifier type
 */
public abstract class AbstractProtocolDataSet<M, F extends Field<M>, ID> extends NamedAndTaggedIdentifyAware<String>
        implements ProtocolDataSet<M, F, ID> {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final Resource resource;
    private Resource cachedResource;
    private Resource cacheDirectory;

    public AbstractProtocolDataSet(Resource resource) {
        requireNonNull(resource);
        this.resource = resource;
        setId(resource.getId());
        setName(resource.getName());
        setDescription(resource.getDescription());
    }

    @Override
    public final Resource getResource() throws IOException {
        if (resource.isDirectory()) {
            return resource;
        } else {
            if (cachedResource == null) downloadResource();
            return cachedResource;
        }
    }

    protected final File getFile() throws IOException {
        return ResourceUtils.toFile(getResource());
    }

    private void downloadResource() throws IOException {
        if (cachedResource == null) {
            String resourceId = Hashing.get(resource.toURI()) + "." + resource.getFileExtension();
            cachedResource = getCacheDirectory().resolve(resourceId, Resource.Type.FILE);
            if (!cachedResource.exists()) {
                LOGGER.info("Downloading data set {} to cache directory", resource.toURI());
                try {
                    cachedResource.copyFrom(resource);
                    LOGGER.info("Successfully downloaded data set {}, size is {} bytes", resource.toURI(), formatBytes(cachedResource.length()));
                } catch (IOException e) {
                    ResourceUtils.delete(cachedResource);
                    rethrowException(e);
                }
            }
        }
    }

    private Resource getCacheDirectory() {
        if (cacheDirectory == null) {
            cacheDirectory = Resource.directory(JvmUtils.getCacheDirectory("protocol"));
        }
        return cacheDirectory;
    }
}
