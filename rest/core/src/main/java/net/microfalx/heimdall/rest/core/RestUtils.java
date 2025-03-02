package net.microfalx.heimdall.rest.core;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentLocator;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.heimdall.rest.api.Scenario;
import net.microfalx.heimdall.rest.api.Simulation;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.UriUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Value;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;

import java.net.URI;
import java.util.Collection;

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

    /**
     * Calculates the APDEX score for a time series (of durations) within a scenario (thresholds).
     *
     * @param scenario the scenario
     * @param values   the values to threshold on
     * @return the score
     */
    public static float getApdexScore(Scenario scenario, Collection<Value> values) {
        requireNonNull(scenario);
        requireNonNull(values);
        if (values.isEmpty()) return 1;
        long toleratingThreshold = scenario.getToleratingThreshold().toMillis();
        long frustratingThreshold = scenario.getFrustratingThreshold().toMillis();
        float numberOfSatisfiedUsers = 0;
        float numberOfToleratingUsers = 0;
        for (Value value : values) {
            double valueDouble = value.asDouble();
            if (valueDouble < toleratingThreshold) {
                numberOfSatisfiedUsers++;
            } else if (valueDouble >= toleratingThreshold && valueDouble <= frustratingThreshold) {
                numberOfToleratingUsers++;
            }
        }
        // numberOfFrustratedUsers (above frustrating threshold)
        // would be multiplied by 0 in the formula, which equals 0
        return (float) ((numberOfSatisfiedUsers + (0.5 * numberOfToleratingUsers)) / (float) values.size());
    }
}
