package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentLocator;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.UriUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;

import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

public class RestUtils {

    public static Metrics metrics = Metrics.of("Rest");

    /**
     * Returns the natural identifier for a resource.
     *
     * @param type     the simulation type
     * @param resource the resource
     * @return a non-null instance
     */
    public static String getNaturalId(Simulation.Type type, Resource resource) {
        requireNonNull(type);
        requireNonNull(resource);
        return type.name().toLowerCase() + "_" + Hashing.hash(toIdentifier(resource.getFileName()));
    }

    /**
     * Returns the mime type associated with a simulation type.
     *
     * @param type the type
     * @return the mime type
     */
    public static String getMimeType(Simulation.Type type) {
        return switch (type) {
            case K6 -> MimeType.TEXT_JAVASCRIPT.getValue();
            case JMETER -> MimeType.TEXT_XML.getValue();
            case GATLING -> "text/java";
        };
    }

    /**
     * Prepares the {@link Content} from a library or simulation.
     *
     * @param contentService the content service
     * @param id             the identifier of library or simulation
     * @param type           the type, library or simulation
     * @param resourceUri    the URI of the resource which provides content
     * @param simulationType the simulation type
     * @return the content, null if cannot be resolved
     */
    public static Content prepareContent(ContentService contentService, String id, String type, String resourceUri,
                                         Simulation.Type simulationType) {
        URI contentUri = UriUtils.parseUri(resourceUri);
        if (contentUri != null) {
            Content content = contentService.resolve(ContentLocator.create(id, type, contentUri));
            return content.withMimeType(RestUtils.getMimeType(simulationType));
        } else {
            return null;
        }
    }

    /**
     * Creates a file name based on environment & simulation name.
     *
     * @param environment the environment name
     * @param simulation  the simulation name
     * @param extension   the file extension
     * @return a non-null instance
     */
    public static String getFileName(Nameable environment, Nameable simulation, String extension) {
        requireNonNull(environment);
        requireNonNull(simulation);
        requireNotEmpty(extension);
        return toIdentifier(environment.getName() + "_" + simulation.getName()) + "." + extension;
    }
}
