package net.microfalx.heimdall.infrastructure.api;

import inet.ipaddr.IPAddressString;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.model.Attributes;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.bootstrap.core.utils.HostnameUtils.getServerNameFromHost;
import static net.microfalx.bootstrap.core.utils.HostnameUtils.isHostname;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ExceptionUtils.rethrowException;
import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A class used to provide a server within the infrastructure.
 */
@Slf4j
@ToString
public class Server extends NamedAndTaggedIdentifyAware<String> implements InfrastructureElement {

    public static String ID = "local";
    public static final Server LOCAL = (Server) Server.create("localhost").tag("local").description("The local server")
            .id(Server.ID).build();

    private String hostname;
    private Type type;
    private Set<Service> services = new HashSet<>();
    private Attributes<?> attributes;
    private boolean icmp;
    private ZoneId zoneId;

    public static InetAddress LOCAL_ADDRESS;
    private static volatile Server SERVER;

    public static Server get() {
        if (SERVER == null) {
            SERVER = (Server) create(LOCAL_ADDRESS.getCanonicalHostName()).tag("local").build();
        }
        return SERVER;
    }

    /**
     * Creates a builder for a server.
     *
     * @return a non-null instance
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * Creates a builder for a server.
     *
     * @param hostname the hostname
     * @return a non-null instance
     */
    public static Builder create(String hostname) {
        return new Builder().hostname(hostname);
    }

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
     * Returns the attributes associated with this environment.
     *
     * @return a non-null instance
     */
    public Attributes<?> getAttributes() {
        return attributes;
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
     * Returns whether the server points to the local server.
     *
     * @return {@code true} if local server, {@code false} otherwise
     */
    public boolean isLocal() {
        return ID.equals(getId());
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
     * Returns a collection of services available on this server.
     *
     * @return a non-null instance
     */
    public Set<Service> getServices() {
        return unmodifiableSet(services);
    }

    /**
     * Returns a new server instance with a different hostname.
     *
     * @param hostname the hostname
     * @return a new instance
     */
    public Server withHostname(String hostname) {
        requireNonNull(hostname);
        Server copy = (Server) copy();
        copy.hostname = hostname;
        copy.setId(toIdentifier(hostname));
        return copy;
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

    static {
        try {
            LOCAL_ADDRESS = InetAddress.getLocalHost();
            LOGGER.info("Local hostname is '{}'", LOCAL_ADDRESS.getCanonicalHostName());
        } catch (UnknownHostException e) {
            try {
                LOCAL_ADDRESS = InetAddress.getByName("localhost");
            } catch (UnknownHostException ex) {
                rethrowException(e);
            }
        }
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
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private String hostname;
        private Type type = Type.VIRTUAL;
        private boolean icmp = true;
        private final Set<Service> services = new HashSet<>();
        private final Attributes<?> attributes = Attributes.create();
        private ZoneId zoneId = ZoneId.systemDefault();

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder hostname(String hostname) {
            requireNotEmpty(hostname);
            this.hostname = hostname;
            return this;
        }

        public Builder icmp(boolean icmp) {
            this.icmp = icmp;
            return this;
        }

        public Builder zoneId(ZoneId zoneId) {
            requireNonNull(zoneId);
            this.zoneId = zoneId;
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

        public Builder attribute(String name, Object value) {
            attributes.add(name, value);
            return this;
        }

        public Builder attributes(Attributes<?> attributes) {
            attributes.copyFrom(attributes);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Server();
        }

        @Override
        protected String updateId() {
            IPAddressString ipAddressString = new IPAddressString(hostname);
            if (ipAddressString.isLoopback()) {
                this.hostname = "localhost";
                return ID;
            } else {
                return toIdentifier(hostname);
            }
        }

        private void updateServer(Server server) {
            if (icmp) server.services.add(Service.create(Service.Type.ICMP));
        }

        @Override
        public Server build() {
            if (isEmpty(hostname)) throw new IllegalArgumentException("Hostname is required");
            Server server = (Server) super.build();
            server.hostname = hostname;
            server.type = type;
            server.icmp = icmp;
            server.zoneId = zoneId;
            server.attributes = attributes;
            server.services = services;
            if (server.isLocal()) {
                server.setName("Local");
            } else if (isEmpty(this.name())) {
                if (isHostname(hostname)) {
                    server.setName(getServerNameFromHost(hostname));
                } else {
                    server.setName(hostname);
                }
            }
            updateServer(server);
            return server;
        }
    }


}
