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
public class Environment extends IdentifiableNameAware<String> {

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
     * Replaces the variables present in this environment.
     * <p>
     * Variables can be accessed by using the placeholder <code>${name}</code>. Parameters are case-insensitive
     *
     * @param text the text with variables
     * @return the text with all variables replaced
     */
    public String replaceVariables(String text) {
        if (text == null || !text.contains("${")) return text;
        for (String parameterName : attributes.getNames()) {
            String value = attributes.get(parameterName).asString();
            text = StringUtils.replaceAll(text, "${" + parameterName.toLowerCase() + "}", value);
        }
        return text;
    }

    /**
     * A builder class.
     */
    public static class Builder extends IdentifiableNameAware.Builder<String> {

        private final Attributes<?> attributes = Attributes.create();
        private final Set<Cluster> clusters = new HashSet<>();
        private final Set<Server> servers = new HashSet<>();

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
