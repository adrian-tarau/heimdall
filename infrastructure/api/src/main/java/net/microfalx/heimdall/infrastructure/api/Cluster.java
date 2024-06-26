package net.microfalx.heimdall.infrastructure.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@ToString
public class Cluster extends NamedAndTaggedIdentifyAware<String> implements InfrastructureElement {

    private ZoneId zoneId;
    private Set<Server> servers;

    /**
     * Returns the type of servers this cluster has.
     *
     * @return a non-null instance
     */
    public Server.Type getType() {
        if (servers.isEmpty()) {
            return Server.Type.VIRTUAL;
        } else {
            return servers.iterator().next().getType();
        }
    }

    /**
     * Returns the server which support this cluster.
     *
     * @return a non-null instance
     */
    public Set<Server> getServers() {
        return unmodifiableSet(servers);
    }

    /**
     * Returns whether the environment uses a given server.
     *
     * @param server the server
     * @return {@code true} if the server is used, {@code false} otherwise
     */
    public boolean hasServer(Server server) {
        requireNonNull(server);
        for (Server registered : servers) {
            if (registered.equals(server)) return true;
        }
        return false;
    }

    /**
     * Returns the zone of the cluster.
     *
     * @return a non-null instance
     */
    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * A builder class.
     */
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private final Set<Server> servers = new HashSet<>();
        private ZoneId zoneId = ZoneId.systemDefault();

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

        public Builder zoneId(ZoneId zoneId) {
            requireNonNull(zoneId);
            this.zoneId = zoneId;
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
            cluster.zoneId = zoneId;
            return cluster;
        }
    }

}
