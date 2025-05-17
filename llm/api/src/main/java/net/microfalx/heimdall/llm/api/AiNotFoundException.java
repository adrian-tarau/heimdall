package net.microfalx.heimdall.llm.api;

/**
 * An exception for indicating that a requested resource was not found.
 */
public class AiNotFoundException extends AiException {

    public AiNotFoundException(String message) {
        super(message);
    }

    public AiNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
