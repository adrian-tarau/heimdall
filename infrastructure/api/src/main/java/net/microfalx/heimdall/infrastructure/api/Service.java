package net.microfalx.heimdall.infrastructure.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.UriUtils;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireBounded;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@ToString
public class Service extends NamedAndTaggedIdentifyAware<String> implements InfrastructureElement {

    private int port;
    private String path;
    private Type type;

    private AuthType authType;
    private String userName;
    private String password;
    private String token;

    private Duration connectionTimeout;
    private Duration readTimeout;
    private Duration writeTimeout;

    /**
     * Creates common service types.
     *
     * @param type the type
     * @return a service instance
     */
    public static Service create(Service.Type type) {
        Builder builder = new Builder().type(type);
        switch (type) {
            case HTTP -> builder.name("Web Server")
                    .description("The home page of the web server over HTTPs");
            case SSH -> builder.name("Shell")
                    .description("The secure shell for the server");
            case ICMP -> builder.name("Ping")
                    .description("The ICMP protocol used to check if the server is available in the network (and latency)");
            default -> throw new InfrastructureException("Unsupported type: " + type);
        }
        return builder.build();
    }

    /**
     * Returns the port at which the service receives requests.
     *
     * @return a positive integer
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the path at which the service receives requests.
     * <p>
     * It only applies to some service types, like {@link Type#HTTP} or {@link Type#HTTPS}
     *
     * @return the path, null if there is no path (root or does not apply)
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the type of this service.
     *
     * @return a non-null instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the authentication to be used to access the service.
     *
     * @return a non-null instance
     */
    public AuthType getAuthType() {
        return authType;
    }

    /**
     * Returns the username to be used with {@link AuthType#BASIC} authentication.
     *
     * @return the username, null if not provided
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the password to be used with {@link AuthType#BASIC} authentication.
     *
     * @return the username, null if not provided
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the token to be used with {@link AuthType#BEARER} or {@link AuthType#API_KEY} authentication.
     *
     * @return the token, null if not provided
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the timeout used to establish connections with the service.
     *
     * @return a non-null instance
     */
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Returns the timeout used to read data from the service.
     *
     * @return a non-null instance
     */
    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * Returns the timeout used to write data to the service.
     *
     * @return a non-null instance
     */
    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * Returns the URI to access the service deployed on a given server within an environment.
     * <p>
     * If an environment is available, the environment variables will be applied against the service.
     *
     * @param server      the server
     * @param environment the environment, optional
     * @return a non-null instance
     */
    public URI getUri(Server server, Environment environment) {
        requireNonNull(server);
        Service newService = this;
        if (environment != null) newService = as(environment);
        StringBuilder builder = new StringBuilder();
        builder.append(newService.getType().getProtocol()).append("://").append(server.getHostname())
                .append(':').append(newService.getPort());
        if (newService.getPath() != null) builder.append(StringUtils.addStartSlash(newService.getPath()));
        return UriUtils.parseUri(builder.toString());
    }

    /**
     * Creates a new instance of the service with variables replaces from a given environment.
     *
     * @param environment the environment
     * @return a new instance
     */
    public Service as(Environment environment) {
        Service copy = (Service) copy();
        copy.path = environment.replaceVariables(path);
        copy.userName = environment.replaceVariables(userName);
        copy.password = environment.replaceVariables(password);
        copy.token = environment.replaceVariables(token);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service service)) return false;
        if (!super.equals(o)) return false;
        return port == service.port && type == service.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), port, type);
    }

    /**
     * An enum for the authentication type.
     */
    public enum AuthType {

        /**
         * There is no authentication to access the service
         */
        NONE,

        /**
         * Basic authentication (for HTTP) or simple username + password for others
         */
        BASIC,

        /**
         * Bearer authentication (for HTTP) or token based for others.
         */
        BEARER,

        /**
         * API KEY authentication (for HTTP)
         */
        API_KEY
    }

    /**
     * A type for a service
     */
    public enum Type {

        /**
         * A service accessed over ICMP (ping).
         */
        ICMP("icmp", -1),

        /**
         * A service accessed over HTTPs.
         */
        HTTP("http", 443),

        /**
         * A service accessed over SSH.
         */
        SSH("ssh", 22),

        /**
         * A generic TCP service.
         */
        TCP("tcp", -1),

        /**
         * A generic UDP service.
         */
        UDP("udp", -1);

        private final String protocol;
        private int port;

        Type(String protocol, int port) {
            this.protocol = protocol;
            this.port = port;
        }

        /**
         * Returns the protocol associated with this service type.
         *
         * @return a non-null instance
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * Returns the default port for the protocol.
         *
         * @return a positive value if it has a default port, -1 for unknown/undefined
         */
        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Type.class.getSimpleName() + "[", "]")
                    .add("protocol='" + protocol + "'")
                    .add("name='" + name() + "'")
                    .toString();
        }
    }

    /**
     * A builder class.
     */
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private int port;
        private String path;
        private Type type = Type.TCP;

        private AuthType authType = AuthType.NONE;
        private String userName;
        private String password;
        private String token;

        private Duration connectionTimeout = Duration.ofSeconds(2);
        private Duration readTimeout = connectionTimeout;
        private Duration writeTimeout = connectionTimeout;

        public Builder(String id) {
            super(id);
        }

        public Builder() {
        }

        public Builder port(int port) {
            if (port != -1) requireBounded(port, 1, 65535);
            this.port = port;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder type(Type type) {
            requireNonNull(type);
            this.type = type;
            this.port = type.getPort();
            return this;
        }

        public Builder authType(AuthType authType) {
            requireNonNull(authType);
            this.authType = authType;
            return this;
        }

        public Builder user(String userName, String password) {
            requireNonNull(userName);
            this.authType = AuthType.BASIC;
            this.userName = userName;
            return this;
        }

        public Builder apiKey(String apiKey) {
            requireNonNull(apiKey);
            this.token = apiKey;
            this.authType = AuthType.API_KEY;
            return this;
        }

        public Builder token(String token) {
            requireNonNull(token);
            this.token = token;
            this.authType = AuthType.BEARER;
            return this;
        }

        public Builder connectionTimeout(Duration connectionTimeout) {
            requireNonNull(connectionTimeout);
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            requireNonNull(readTimeout);
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(Duration writeTimeout) {
            requireNonNull(writeTimeout);
            this.writeTimeout = writeTimeout;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Service();
        }

        protected String updateId() {
            StringBuilder builder = new StringBuilder();
            builder.append(type.name().toLowerCase());
            if (port > 0) builder.append('_').append(port);
            if (path != null) builder.append('_').append(StringUtils.toIdentifier(path));
            return builder.toString();
        }

        private void validate() {
            if (port == 0) throw new IllegalArgumentException("Port is required");
        }

        @Override
        public Service build() {
            validate();
            Service service = (Service) super.build();
            service.port = port;
            service.type = type;
            service.path = path;
            service.authType = authType;
            service.userName = userName;
            service.password = password;
            service.token = token;
            service.connectionTimeout = connectionTimeout;
            service.readTimeout = readTimeout;
            service.writeTimeout = writeTimeout;
            return service;
        }
    }

}
