package net.microfalx.heimdall.llm.api;

/**
 * Base class for all exceptions thrown by the AI framework.
 */
public class LlmException extends RuntimeException {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
