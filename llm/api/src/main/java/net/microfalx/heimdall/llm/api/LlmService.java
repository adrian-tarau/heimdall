package net.microfalx.heimdall.llm.api;

import net.microfalx.resource.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;

/**
 * A service which manages the AI models and providers.
 */
@Service
public interface LlmService {

    /**
     * Creates a chat session with the default model and an empty prompt.
     *
     * @return a non-null instance
     */
    Chat createChat();

    /**
     * Creates a chat session with the default model.
     *
     * @param prompt the prompt to use
     * @return a non-null instance
     */
    Chat createChat(Prompt prompt);

    /**
     * Creates a chat session with a model and an empty prompt.
     *
     * @param model the prompt to use
     * @return a non-null instance
     */
    Chat createChat(Model model);

    /**
     * Creates a chat session using a given model.
     *
     * @param prompt the prompt to use
     * @param model  the model to use
     * @return a non-null instance
     */
    Chat createChat(Prompt prompt, Model model);

    /**
     * Returns the chat sessions for a given user.
     *
     * @param principal the user principal
     * @param active {@code true} to return only active chats, {@code false} to return previous chats
     * @return a non-null instance
     */
    Collection<Chat> getChats(Principal principal, boolean active);

    /**
     * Returns a chat session with a given identifier.
     *
     * @param id the chat session identifier
     * @return a non-null instance
     */
    Chat getChat(String id);

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
     * Summarizes the text content using the default model.
     *
     * @param text the text to summarize.
     * @return the summary of the text.
     */
    String summarize(String text, boolean shortMessage);

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
     * Returns registered tools.
     *
     * @return a non-null instance
     */
    Collection<Tool> getTools();

    /**
     * Returns a tool with a given identifier.
     *
     * @param id the tool identifier
     * @return a non-null instance
     */
    Tool getTool(String id);

    /**
     * Registers a tool.
     *
     * @param tool the tool to register
     */
    void registerTool(Tool tool);

    /**
     * Registers a variable that can be used in prompts.
     * <p>
     * The content of the variable will be loaded from a classpath resource available at ~/llm/variables/{name}.md.
     *
     * @param name the name of the variable
     */
    void registerVariable(String name);

    /**
     * Registers a variable that can be used in prompts.
     *
     * @param name  the name of the variable
     * @param value the value of the variable
     */
    void registerVariable(String name, Object value);

    /**
     * Applies a template to a resource, replacing variables with their values.
     * <p>
     * All registers variables will be available for replacement.
     *
     * @param resource  the resource to apply the template to
     * @param variables the variables to replace in the template
     * @return the rendered template
     * @see #registerVariable(String, Object)
     */
    Resource applyTemplate(Resource resource, Map<String, Object> variables) throws IOException;

    /**
     * Reloads the models.
     */
    void reload();

    /**
     * Returns the default prompt, to be used when no prompt is specified.
     *
     * @return a non-null instance
     */
    Prompt getDefaultPrompt();

    /**
     * Returns registered prompts.
     *
     * @return a non-null instance
     */
    Collection<Prompt> getPrompts();

    /**
     * Returns registered prompts which have at least one of the given tags.
     *
     * @return a non-null instance
     */
    Collection<Prompt> getPrompts(Collection<String> tags);

    /**
     * Returns registered prompts which have at least one of the given tags.
     *
     * @return a non-null instance
     */
    Collection<Prompt> getPrompts(String... tags);

    /**
     * Returns a prompt with a given identifier.
     *
     * @param id the prompt identifier
     * @return a non-null instance
     */
    Prompt getPrompt(String id);

    /**
     * Registers a prompt.
     *
     * @param prompt the prompt to register
     */
    void registerPrompt(Prompt prompt);
}
