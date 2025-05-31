package net.microfalx.heimdall.llm.api;

import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * A service which manages the AI models and providers.
 */
@Service
public interface LlmService extends Chat.Factory {

    /**
     * Creates a chat session with the default model.
     *
     * @return a non-null instance
     */
    Chat createChat();

    /**
     * Creates a chat session using a given model.
     *
     * @param modelId the model identifier to use
     * @return a non-null instance
     */
    Chat createChat(String modelId);

    /**
     * Embed the text content with the default model.
     *
     * @param text the text to embed.
     * @return the embedding.
     */
    Embedding embed(String text);

    /**
     * Embed the text content using a given model.
     *
     * @param modelId the model to use
     * @param text    the text to embed.
     * @return the embedding.
     */
    Embedding embed(String modelId, String text);

    /**
     * Returns the default model.
     *
     * @return a non-null instance
     */
    Model getDefaultModel();

    /**
     * Returns the default model used for embedding.
     *
     * @return a non-null instance
     */
    Model getDefaultEmbeddingModel();

    /**
     * Returns registered chat models.
     *
     * @return a non-null instance
     */
    Collection<Model> getModels();

    /**
     * Returns the model with a given identifier.
     *
     * @param id the model identifier
     * @return the model
     * @throws LlmNotFoundException if the model cannot be found
     */
    Model getModel(String id);

    /**
     * Returns the active chat sessions.
     *
     * @return a non-null instance
     */
    Collection<Chat> getActiveChats();

    /**
     * Returns the historical chat sessions.
     *
     * @return a non-null instance
     */
    Iterable<Chat> getHistoricalChats();

    /**
     * Returns registered providers.
     *
     * @return a non-null instance
     */
    Collection<Provider> getProviders();

    /**
     * Registers a provider.
     *
     * @param provider the provider
     */
    void registerProvider(Provider provider);

    /**
     * Reloads the models.
     */
    void reload();
}
