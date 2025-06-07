package net.microfalx.heimdall.llm.core;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.heimdall.llm.api.Provider;

/**
 * Base class for all provider factories.
 */
public abstract class AbstractProviderFactory implements Provider.Factory {

    /**
     * Properties to be used with the provider factory.
     */
    @Getter
    @Setter
    private LlmProperties properties = new LlmProperties();
}
