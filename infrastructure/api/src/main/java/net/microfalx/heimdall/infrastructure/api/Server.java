package net.microfalx.heimdall.infrastructure.api;

import lombok.ToString;
import net.microfalx.lang.IdentifiableNameAware;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.StringUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class used to provide a server within the infrastructure.
 */
@ToString
public class Server extends IdentifiableNameAware<String> {

    private String hostname;
    private Type type;
    private Set<Service> services = new HashSet<>();
    private boolean icmp;

    /**
     * Returns the hostname of the server
     *
     * @return a non-null instance
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the type for this server.
     *
     * @return a non-null instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns whether the ICMP "service" is available.
     * <p>
     * When available, the service can be pinged over ICMP.
     *
     * @return {@code true} if ping is available, {@code false} otherwise
     */
    public boolean isIcmp() {
        return icmp;
    }

    /**
     * Returns a collection of services available on this server.
     *
     * @return a non-null instance
     */
    public Set<Service> getServices() {
        return unmodifiableSet(services);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Server server)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(hostname, server.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hostname);
    }

    /**
     * An enum for the server type
     */
    public enum Type {

        /**
         * The server is physical.
         */
        PHYSICAL,

        /**
         * The server is virtual (a virtual machine).
         */
        VIRTUAL
    }

    /**
     * A builder class.
     */
    public static class Builder extends IdentifiableNameAware.Builder<String> {

        private String hostname;
        private Type type = Type.VIRTUAL;
        private boolean icmp = true;
        private final Set<Service> services = new HashSet<>();

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder icmp(boolean icmp) {
            this.icmp = icmp;
            return this;
        }

        public Builder type(Type type) {
            requireNonNull(type);
            this.type = type;
            return this;
        }

        public Builder service(Service service) {
            requireNonNull(service);
            this.services.add(service);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Server();
        }

        @Override
        protected String updateId() {
            return hostname;
        }

        private void updateServer(Server server) {
            if (icmp) {
                server.services.add(Service.create(Service.Type.ICMP));
            }
        }

        @Override
        public Server build() {
            if (StringUtils.isEmpty(hostname)) throw new IllegalArgumentException("Hostname is required");
            Server server = (Server) super.build();
            server.hostname = hostname;
            server.type = type;
            server.services = services;
            updateServer(server);
            return server;
        }
    }


}
