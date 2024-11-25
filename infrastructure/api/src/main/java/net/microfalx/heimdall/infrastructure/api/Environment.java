package net.microfalx.heimdall.infrastructure.api;

import lombok.ToString;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.UriUtils;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.UriUtils.SLASH;
import static net.microfalx.lang.UriUtils.parseUri;

@ToString
public class Environment extends NamedAndTaggedIdentifyAware<String> implements InfrastructureElement {

    private Attributes<?> attributes;
    private Set<Cluster> clusters;
    private Set<Server> servers;

    private URI baseUri;
    private String appPath;
    private String apiPath;
    private String version;

    /**
     * Creates a builder for an environment.
     *
     * @return a non-null instance
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * Creates a builder for an environment.
     *
     * @param id the environment identifier
     * @return a non-null instance
     */
    public static Builder create(String id) {
        return new Builder(id);
    }

    /**
     * Returns the attributes associated with this environment.
     *
     * @param all {@code true} to return all the attributes, user provided and environment, {@code false} only for user attributes
     * @return a non-null instance
     */
    public Attributes<?> getAttributes(boolean all) {
        if (all) {
            Attributes<Attribute> allAttributes = Attributes.create(this.attributes);
            updateAttributes(allAttributes);
            return allAttributes;
        } else {
            return attributes;
        }
    }

    /**
     * Returns the base URI (most of the time it will be a URL).
     *
     * @return a non-null
     */
    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Returns the application URI (most of the time it will be a URL and HTTP protocol), which in most cases is the user interface.
     *
     * @return a non-null
     */
    public URI getAppUri() {
        return UriUtils.appendPath(baseUri, apiPath);
    }

    /**
     * Returns the API URI (most of the time it will be a URL and HTTP protocol), which in most cases is the Rest API.
     *
     * @return a non-null
     */
    public URI getApiUri() {
        return UriUtils.appendPath(baseUri, apiPath);
    }

    /**
     * Returns the application path, relative to the base URI.
     *
     * @return a non-null instance
     * @see #getBaseUri()
     */
    public String getAppPath() {
        return appPath;
    }

    /**
     * Returns the API path, relative to the base URI.
     *
     * @return a non-null instance
     * @see #getBaseUri()
     */
    public String getApiPath() {
        return apiPath;
    }

    /**
     * Returns the clusters part of this environment.
     *
     * @return a non-null instance
     */
    public Set<Cluster> getClusters() {
        return unmodifiableSet(clusters);
    }

    /**
     * Returns the servers part of this environment.
     *
     * @return a non-null instance
     */
    public Set<Server> getServers() {
        return unmodifiableSet(servers);
    }

    /**
     * Return the version of the environment
     *
     * @return a non-null instance
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns all servers part of this environment.
     * <p>
     * This method returns a union of all server assigned to this environment, and all servers from all clusters assigned
     * to this environment.
     *
     * @return a non-null instance
     */
    public Set<Server> getAllServers() {
        Set<Server> allServers = new HashSet<>();
        for (Cluster cluster : clusters) {
            allServers.addAll(cluster.getServers());
        }
        allServers.addAll(servers);
        return allServers;
    }

    /**
     * Replaces the variables in the given text.
     * <p>
     * Variables can be accessed by using the placeholder <code>${name}</code>. Parameters are case-insensitive.
     *
     * @param text the text with variables
     * @return the text with all variables replaced
     */
    public String replaceVariables(String text) {
        return attributes.replaceVariables(text);
    }

    /**
     * Returns whether the environment uses a given server.
     *
     * @param server the server
     * @return {@code true} if the server is used, {@code false} otherwise
     */
    public boolean hasServer(Server server) {
        requireNonNull(server);
        for (Cluster cluster : clusters) {
            if (cluster.hasServer(server)) return true;
        }
        for (Server registeredServer : servers) {
            if (registeredServer.equals(server)) return true;
        }
        return false;
    }

    private void updateAttributes(Attributes<?> attributes) {
        attributes.addIfAbsent(BASE_URI, getBaseUri().toASCIIString());
        attributes.addIfAbsent(APP_URI, getAppUri().toASCIIString());
        attributes.addIfAbsent(API_URI, getApiUri().toASCIIString());
        attributes.addIfAbsent(REST_API_URI, getApiUri().toASCIIString());
    }

    private static void updateDefaultAttributes(Attributes<?> attributes) {
        attributes.addIfAbsent(USERNAME_VARIABLE, EMPTY_STRING);
        attributes.addIfAbsent(PASSWORD_VARIABLE, EMPTY_STRING);
        attributes.addIfAbsent(BEARER_VARIABLE, EMPTY_STRING);
        attributes.addIfAbsent(API_KEY_VARIABLE, EMPTY_STRING);
    }

    /**
     * A builder class.
     */
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private final Attributes<?> attributes = Attributes.create();
        private final Set<Cluster> clusters = new HashSet<>();
        private final Set<Server> servers = new HashSet<>();

        private String baseUri;
        private String appPath;
        private String apiPath;
        private String version;

        public Builder(String id) {
            super(id);
            updateDefaultAttributes(attributes);
        }

        public Builder() {
            updateDefaultAttributes(attributes);
        }

        @Override
        protected IdentityAware<String> create() {
            return new Environment();
        }

        public Builder attribute(String name, Object value) {
            attributes.add(name, value);
            return this;
        }

        public Builder attributes(Attributes<?> attributes) {
            attributes.copyFrom(attributes);
            return this;
        }

        public Builder cluster(Cluster cluster) {
            requireNonNull(cluster);
            this.clusters.add(cluster);
            return this;
        }

        public Builder server(Server server) {
            requireNonNull(server);
            this.servers.add(server);
            return this;
        }

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder appPath(String appPath) {
            this.appPath = appPath;
            return this;
        }

        public Builder apiPath(String apiPath) {
            this.apiPath = apiPath;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        @Override
        public Environment build() {
            if (StringUtils.isEmpty(baseUri)) throw new IllegalArgumentException("Base URI is required");
            Environment environment = (Environment) super.build();
            environment.attributes = attributes;
            environment.clusters = clusters;
            environment.servers = servers;
            environment.baseUri = parseUri(baseUri);
            environment.appPath = defaultIfEmpty(appPath, SLASH);
            environment.apiPath = defaultIfEmpty(apiPath, SLASH);
            environment.version = version;
            return environment;
        }
    }

}
