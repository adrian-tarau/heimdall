package net.microfalx.heimdall.llm.core;

import dev.langchain4j.data.embedding.Embedding;

/**
 * Various utility methods for working with LLMs.
 */
public class LlmUtils {

    /**
     * Unwraps the given embedding to its underlying implementation.
     *
     * @param embedding the embedding to unwrap
     * @return the underlying embedding instance
     * @throws IllegalArgumentException if the embedding is not the expected implementation
     */
    public static Embedding unwrap(net.microfalx.heimdall.llm.api.Embedding embedding) {
        if (embedding instanceof AbstractEmbeddingFactory.EmbeddingImpl) {
            return ((AbstractEmbeddingFactory.EmbeddingImpl) embedding).embedding;
        } else {
            throw new IllegalArgumentException("Unsupported embedding type: " + embedding.getClass().getName());
        }
    }
}
