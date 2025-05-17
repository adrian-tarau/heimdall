package net.microfalx.heimdall.llm.api;

/**
 * A factory for creating chat sessions.
 */
public interface ChatFactory {

    /**
     * Creates a chat session with a given identifier.
     *
     * @param model the model to use
     * @return a non-null instance
     */
    Chat createChat(Model model);
}
