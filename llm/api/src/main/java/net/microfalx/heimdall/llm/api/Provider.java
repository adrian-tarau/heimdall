package net.microfalx.heimdall.llm.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An interface for representing an AI model provider.
 */
@ToString(callSuper = true)
public class Provider extends NamedAndTaggedIdentifyAware<String> {

    private URI uri;
    private String apyKey;

    @ToString.Exclude
    private List<Model> models;
    private Chat.Factory chatFactory;

    private String version = StringUtils.NA_STRING;
    private String author = StringUtils.NA_STRING;
    private String license = "Proprietary";

    /**
     * Returns the URI of the provider.
     * <p>
     * A remote provider will use the same URI for all models.
     *
     * @return the URI if the provider, null if the model is accessed over an API
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the API key to use when accessing the model.
     *
     * @return the apy key, null if the model is accessed over an API
     * @see Model#getUri()
     */
    public String getApyKey() {
        return apyKey;
    }

    /**
     * Returns the version of the provider.
     *
     * @return the version string, never null
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the author of the provider.
     *
     * @return the author string, never null
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the license of the provider.
     *
     * @return the license string, never null
     */
    public String getLicense() {
        return license;
    }

    /**
     * Returns all models provided by this provider.
     *
     * @return a non-null instance
     */
    public Collection<Model> getModels() {
        return unmodifiableList(models);
    }

    /**
     * Returns a model.
     *
     * @param id the identifier of the model
     * @return the model
     * @throws LlmNotFoundException if the model was not found
     */
    public Model getModel(String id) {
        for (Model model : models) {
            if (model.getId().equals(id)) return model;
        }
        throw new LlmNotFoundException("A model with identifier " + id + " was not found");
    }

    /**
     * Returns the factory used to create chat sessions.
     *
     * @return a non-null instance
     */
    public Chat.Factory getChatFactory() {
        return chatFactory;
    }

    /**
     * A factory for creating providers.
     */
    public interface Factory {

        /**
         * Creates a provider.
         *
         * @return a non-null instance
         */
        Provider createProvider();
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private URI uri;
        private String apyKey;

        private String version = StringUtils.NA_STRING;
        private String author = StringUtils.NA_STRING;
        private String license = "Proprietary";

        private final List<Model.Builder> models = new ArrayList<>();
        private Chat.Factory chatFactory;

        public Builder(String id) {
            super(id);
        }

        @Override
        protected IdentityAware<String> create() {
            return new Provider();
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder uri(URI uri, String apyKey) {
            requireNonNull(uri);
            requireNonNull(apyKey);
            this.uri = uri;
            this.apyKey = apyKey;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder license(String license) {
            this.license = license;
            return this;
        }

        public Builder model(Model.Builder model) {
            requireNonNull(model);
            models.add(model);
            return this;
        }

        public Builder chatFactory(Chat.Factory chatFactory) {
            requireNonNull(chatFactory);
            this.chatFactory = chatFactory;
            return this;
        }

        @Override
        public Provider build() {
            if (chatFactory == null) throw new IllegalArgumentException("Chat factory cannot be null");
            Provider provider = (Provider) super.build();
            provider.uri = uri;
            provider.apyKey = apyKey;
            provider.version = version;
            provider.author = author;
            provider.license = license;
            provider.chatFactory = chatFactory;
            provider.models = models.stream().map(builder -> builder.provider(provider).build()).toList();
            return provider;
        }
    }
}
