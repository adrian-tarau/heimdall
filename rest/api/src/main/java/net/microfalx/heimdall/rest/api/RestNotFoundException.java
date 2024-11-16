package net.microfalx.heimdall.rest.api;

/**
 * An exception for a rest element not being found.
 */
public class RestNotFoundException extends RestException {

    public RestNotFoundException(String message) {
        super(message);
    }
}
