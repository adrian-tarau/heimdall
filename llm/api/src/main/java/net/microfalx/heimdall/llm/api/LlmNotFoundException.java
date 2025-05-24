package net.microfalx.heimdall.llm.api;

/**
 * An exception for indicating that a requested resource was not found.
 */
public class LlmNotFoundException extends LlmException {

    public LlmNotFoundException(String message) {
        super(message);
    }

    public LlmNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
