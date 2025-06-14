package net.microfalx.heimdall.llm.api;

/**
 * An interface representing the token usage of a model call.
 */
public interface TokenUsage {

    /**
     * Returns the input token count, or null if unknown.
     *
     * @return the input token count, or null if unknown.
     */
    Integer getInputTokenCount();

    /**
     * Returns the output token count, or null if unknown.
     *
     * @return the output token count, or null if unknown.
     */
    Integer getOutputTokenCount();

}
