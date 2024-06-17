package net.microfalx.heimdall.infrastructure.api;

import lombok.ToString;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.lang.IdentifiableNameAware;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@ToString
public class Environment extends IdentifiableNameAware<String> implements InfrastructureElement {

    private Attributes<?> attributes;
    private Set<Cluster> clusters;
    private Set<Server> servers;

    /**
     * Returns the attributes associated with this environment.
     *
     * @return a non-null instance
     */
    public Attributes<?> getAttributes() {
        return attributes;
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

    private static void updateDefaultAttributes(Attributes<?> attributes) {
        attributes.add(InfrastructureConstants.USERNAME_VARIABLE, StringUtils.EMPTY_STRING);
        attributes.add(InfrastructureConstants.PASSWORD_VARIABLE, StringUtils.EMPTY_STRING);
        attributes.add(InfrastructureConstants.BEARER_VARIABLE, StringUtils.EMPTY_STRING);
        attributes.add(InfrastructureConstants.API_KEY_VARIABLE, StringUtils.EMPTY_STRING);
    }

    /**
     * A builder class.
     */
    public static class Builder extends IdentifiableNameAware.Builder<String> {

        private final Attributes<?> attributes = Attributes.create();
        private final Set<Cluster> clusters = new HashSet<>();
        private final Set<Server> servers = new HashSet<>();

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

        @Override
        public Environment build() {
            Environment environment = (Environment) super.build();
            environment.attributes = attributes;
            environment.clusters = clusters;
            environment.servers = servers;
            return environment;
        }
    }

}
