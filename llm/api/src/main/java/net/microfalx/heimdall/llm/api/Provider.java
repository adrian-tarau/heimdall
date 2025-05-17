package net.microfalx.heimdall.llm.api;

import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An interface for representing an AI model provider.
 */
public class Provider extends NamedAndTaggedIdentifyAware<String> {

    private URI uri;
    private String apyKey;

    private List<Model> models;
    private ChatFactory chatFactory;

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
     * Returns all models provided by this provider.
     *
     * @return a non-null instance
     */
    public Collection<Model> getModels() {
        return Collections.unmodifiableList(models);
    }

    /**
     * Returns the factory used to create chat sessions.
     *
     * @return a non-null instance
     */
    public ChatFactory getChatFactory() {
        return chatFactory;
    }

    protected void updateModels(Collection<Model> models) {
        requireNonNull(models);
        this.models = new ArrayList<>(models);
        for (Model model : models) {
            model.provider = this;
        }
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private URI uri;
        private String apyKey;

        private final List<Model> models = new ArrayList<>();
        private ChatFactory chatFactory;

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
            return this;
        }

        public Builder model(Model model) {
            requireNonNull(model);
            models.add(model);
            return this;
        }

        public void chatFactory(ChatFactory chatFactory) {
            requireNonNull(chatFactory);
            this.chatFactory = chatFactory;
        }

        @Override
        public Provider build() {
            if (chatFactory == null) throw new IllegalArgumentException("Chat factory cannot be null");
            Provider provider = (Provider) super.build();
            provider.uri = uri;
            provider.apyKey = apyKey;
            provider.chatFactory = chatFactory;
            provider.updateModels(models);
            return provider;
        }
    }
}
