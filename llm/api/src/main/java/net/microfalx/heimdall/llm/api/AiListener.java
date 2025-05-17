package net.microfalx.heimdall.llm.api;

/**
 * A listener interface for registering.
 */
public interface AiListener {

    /**
     * Invoked during the application startup to register all available providers.
     */
    void registerProviders(AiService service);
}
