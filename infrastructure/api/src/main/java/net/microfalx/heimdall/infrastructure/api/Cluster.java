package net.microfalx.heimdall.infrastructure.api;

import lombok.ToString;
import net.microfalx.lang.IdentifiableNameAware;
import net.microfalx.lang.IdentityAware;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@ToString
public class Cluster extends IdentifiableNameAware<String> {

    private Set<Server> servers;

    /**
     * Returns the server which support this cluster.
     *
     * @return a non-null instance
     */
    public Set<Server> getServers() {
        return unmodifiableSet(servers);
    }

    /**
     * A builder class.
     */
    public static class Builder extends IdentifiableNameAware.Builder<String> {

        private final Set<Server> servers = new HashSet<>();

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder server(Server server) {
            requireNonNull(server);
            this.servers.add(server);
            return this;
        }

        public Builder servers(Collection<Server> servers) {
            requireNonNull(servers);
            this.servers.addAll(servers);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Cluster();
        }

        @Override
        public Cluster build() {
            Cluster cluster = (Cluster) super.build();
            cluster.servers = servers;
            return cluster;
        }
    }

}
