package net.microfalx.heimdall.rest.api;

import net.microfalx.lang.Hashing;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

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

    public static Builder create(URI uri) {
        return new Builder(uri);
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

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private final URI uri;
        private String userName;
        private String password;
        private String token;

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
            project.userName = userName;
            project.password = password;
            project.token = token;
            return project;
        }
    }
}
