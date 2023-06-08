package net.microfalx.heimdall.protocol.core;

/**
 * Base exception for all protocols.
 */
public class ProtocolException extends RuntimeException{

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
