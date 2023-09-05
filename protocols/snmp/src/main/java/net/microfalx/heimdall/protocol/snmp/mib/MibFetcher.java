package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.resource.NullResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import net.microfalx.resource.UrlResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

import static java.lang.Character.toUpperCase;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Searches for a MIB on websites hosting collection of MIBs.
 */
class MibFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MibFetcher.class);

    private String moduleId;

    private String[] locations = new String[]{
            "https://www.circitor.fr/Mibs/Mib/${MODULE_PREFIX}/${MODULE}.mib",
            "https://bestmonitoringtools.com/mibdb/mibs/${MODULE}.mib"
    };

    public static MibFetcher create(String moduleId) {
        return new MibFetcher(moduleId);
    }

    private MibFetcher(String moduleId) {
        requireNonNull(moduleId);
        this.moduleId = moduleId;
    }

    /**
     * Tries to find a MIB from external resources.
     *
     * @return the resource
     * @throws IOException an I/O if the resource cannot be located or downloaded
     */
    Resource execute() throws IOException {
        LOGGER.debug("Fetch MIB '{}' from internet", moduleId);
        Resource resource = NullResource.createNull();
        for (String location : locations) {
            resource = fetch(location);
            if (resource.exists()) {
                LOGGER.info("MIB '{}' downloaded from '{}'", moduleId, replacePlaceholders(location));
                break;
            }
            resource = NullResource.createNull();
        }
        return resource;
    }

    private Resource fetch(String location) throws IOException {
        String uri = replacePlaceholders(location);
        LOGGER.debug("Fetch MIB from " + uri);
        Resource remoteResource = UrlResource.create(URI.create(uri));
        if (!remoteResource.exists()) return remoteResource;
        Resource localResource = TemporaryFileResource.file(remoteResource.getFileName());
        localResource.copyFrom(remoteResource);
        return localResource;
    }

    private String replacePlaceholders(String uriPattern) {
        uriPattern = StringUtils.replaceOnce(uriPattern, "${MODULE}", moduleId);
        uriPattern = StringUtils.replaceOnce(uriPattern, "${MODULE_PREFIX}", String.valueOf(toUpperCase(moduleId.charAt(0))));
        return uriPattern;
    }
}
