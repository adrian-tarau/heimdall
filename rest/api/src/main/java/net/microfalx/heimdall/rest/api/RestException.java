package net.microfalx.heimdall.rest.api;

/**
 * Base exception for all Rest exceptions.
 */
public class RestException extends RuntimeException {

    public RestException(String message) {
        super(message);
    }

    public RestException(String message, Throwable cause) {
        super(message, cause);
    }
}
