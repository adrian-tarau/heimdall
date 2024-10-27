package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.*;

import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A project (a source code repository) containing simulations related to the project.
 */
public class Project extends NamedAndTaggedIdentifyAware<String> {

    private URI uri;
    private String userName;
    private String password;
    private String token;
    private Type type;

    private String libraryPath;
    private String simulationPath;

    public static Builder create(URI uri) {
        return new Builder(uri);
    }

    /**
     * Returns the type of the project
     *
     * @return a non-null instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the URI to access the project repository.
     *
     * @return a non-null instance
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns an optional username to authenticate against the repository.
     *
     * @return the username, null if not provided
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns an optional password associated with the username to authenticate against the repository.
     *
     * @return the password, null if not provided
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns an optional access token to authenticate against the repository.
     *
     * @return the access token, null if not provided
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the path within the project which holds the (shared) libraries required for the simulation
     * @return a non-null instance
     */
    public String getLibraryPath() {
        return StringUtils.defaultIfEmpty(libraryPath, UriUtils.SLASH);
    }

    /**
     * Returns the path within the project which holds the simulations.
     *
     * @return a non-null instance
     */
    public String getSimulationPath() {
        return StringUtils.defaultIfEmpty(simulationPath, UriUtils.SLASH);
    }

    public enum Type {
        /**
         * The GIT version control
         */
        GIT,

        /**
         * The SVN version control
         */
        SVN,
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private final URI uri;
        private Type type;
        private String userName;
        private String password;
        private String token;

        private String libraryPath = UriUtils.SLASH;
        private String simulationPath = UriUtils.SLASH;

        public Builder(URI uri) {
            super();
            requireNonNull(uri);
            this.uri = uri;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder type(Type type) {
            requireNonNull(type);
            this.type = type;
            return this;
        }

        public Builder libraryPath(String libraryPath) {
            requireNonNull(libraryPath);
            this.libraryPath = libraryPath;
            return this;
        }

        public Builder simulationPath(String simulationPath) {
            requireNonNull(simulationPath);
            this.simulationPath = simulationPath;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Project();
        }

        @Override
        protected String updateId() {
            return Hashing.hash(uri.toASCIIString());
        }

        @Override
        public NamedAndTaggedIdentifyAware<String> build() {
            Project project = (Project) super.build();
            if (type == null) throw new IllegalArgumentException("The type is require");
            project.type = type;
            project.userName = userName;
            project.password = password;
            project.token = token;
            project.libraryPath = libraryPath;
            project.simulationPath = simulationPath;
            return project;
        }
    }
}
