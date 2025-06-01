package net.microfalx.heimdall.llm.core;

import dev.langchain4j.data.embedding.Embedding;
import net.microfalx.threadpool.ThreadPool;

/**
 * Various utility methods for working with LLMs.
 */
public class LlmUtils {

    static final ThreadLocal<ThreadPool> THREAD_POOL = ThreadLocal.withInitial(ThreadPool::get);

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

    /**
     * Returns the current thread pool to be used for LLM operations.
     *
     * @return a non-null instance
     */
    public static ThreadPool getThreadPool() {
        return THREAD_POOL.get();
    }
}
