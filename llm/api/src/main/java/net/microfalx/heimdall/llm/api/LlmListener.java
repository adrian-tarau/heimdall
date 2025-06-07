package net.microfalx.heimdall.llm.api;

/**
 * A listener interface for {@link LlmService}.
 */
public interface LlmListener {

    /**
     * Invoked when the LLM service is started.
     *
     * @param service the service instance
     */
    default void onStart(LlmService service) {
        // empty default implementation
    }

    /**
     * Invoked when a chat session is started with a prompt to provide context or instructions for the chat.
     * <p>
     * The first listener to return a non-null value will be used to augment the prompt fragment. Each model/provider
     * supports different ways to augment the prompt, and the listener should react only to the models it knows.
     *
     * @param model    the model used for the chat session
     * @param prompt   the prompt used to start the chat session
     * @param fragment the fragment of the prompt that is being processed
     * @param text     the text to augment the prompt fragment with, typically a string containing
     *                 instructions or context and can be null if the original text is not provided
     * @return the augmented text, or null if no augmentation can be applied with this listener
     */
    default String augment(Model model, Prompt prompt, Prompt.Fragment fragment, String text) {
        return null;
    }
}
