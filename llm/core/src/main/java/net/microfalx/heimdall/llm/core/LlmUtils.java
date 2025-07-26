package net.microfalx.heimdall.llm.core;

import dev.langchain4j.data.embedding.Embedding;
import net.microfalx.heimdall.llm.api.LlmService;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.threadpool.ThreadPool;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.SPACE_CHAR;

/**
 * Various utility methods for working with LLMs.
 */
public class LlmUtils {

    private static final char DOT = '.';
    private static final char GRAVE = '`';
    static Metrics ROOT_METRICS = Metrics.of("LLM");
    static Metrics CREATE_CHAT_METRICS = ROOT_METRICS.withGroup("Create Chat");
    static Metrics TOOL_METRICS = ROOT_METRICS.withGroup("Tool");
    static Metrics TOOL_EXECUTION_METRICS = TOOL_METRICS.withGroup("Execution");
    static Metrics CREATE_SYSTEM_MESSAGE_METRICS = Metrics.of("Create System Message");
    static Metrics MISC_METRICS = ROOT_METRICS.withGroup("Misc");

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
        appendDot(builder);
        builder.append(SPACE_CHAR);
        sentence = sentence.trim();
        builder.append(sentence);
        appendDot(builder);
        return builder;
    }

    private static void appendDot(StringBuilder builder) {
        if (builder.isEmpty()) return;
        char lastChar = builder.charAt(builder.length() - 1);
        if (!(lastChar == DOT || lastChar == GRAVE || lastChar == '\n')) {
            builder.append(DOT);
        }
    }

    /**
     * Returns the chat thread pool for the given LLM service.
     *
     * @param service the LLM service
     * @return a non-null thread pool instance
     */

    public static ThreadPool getChatThreadPool(LlmService service) {
        if (service instanceof LlmServiceImpl serviceImpl) {
            return serviceImpl.getChatPool();
        }
        return ThreadPool.get();
    }
}
