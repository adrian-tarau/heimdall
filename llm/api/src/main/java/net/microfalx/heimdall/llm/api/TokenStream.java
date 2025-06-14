package net.microfalx.heimdall.llm.api;

import java.util.Iterator;

/**
 * An interface representing a stream of tokens, typically used in language model processing.
 */
public interface TokenStream extends Iterator<String>, TokenUsage {

    /**
     * Returns the complete message associated with this stream.
     *
     * @return a non-null instance
     */
    Message getMessage();

    /**
     * Returns the finish reason for the stream.
     *
     * @return a non-null instance of {@link FinishReason}
     */
    FinishReason getFinishReason();

    /**
     * Returns when the stream is complete.
     *
     * @return {@code true} if the stream has been fully consumed, {@code false} otherwise.
     */
    boolean isComplete();
}
