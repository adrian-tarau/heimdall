package net.microfalx.heimdall.llm.core;

import dev.langchain4j.data.embedding.Embedding;
import net.microfalx.lang.StringUtils;
import net.microfalx.threadpool.ThreadPool;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Various utility methods for working with LLMs.
 */
public class LlmUtils {

    private static final char DOT = '.';

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

    /**
     * Appends a sentence to the given text, ensuring proper punctuation.
     *
     * @param builder  the builder to append to
     * @param sentence the sentence to append
     * @return the combined text with proper punctuation
     */
    public static StringBuilder appendSentence(StringBuilder builder, String sentence) {
        requireNonNull(builder);
        if (StringUtils.isEmpty(sentence)) return builder;
        if (!builder.isEmpty() && builder.charAt(builder.length() - 1) != DOT) {
            builder.append(DOT);
        }
        builder.append(StringUtils.SPACE_CHAR);
        sentence = sentence.trim();
        builder.append(sentence);
        if (sentence.charAt(sentence.length() - 1) != DOT) {
            builder.append(DOT);
        }
        return builder;
    }
}
