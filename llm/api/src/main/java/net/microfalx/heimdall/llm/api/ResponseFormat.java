package net.microfalx.heimdall.llm.api;

/**
 * Enum representing the format of the response from an AI model.
 * <p>
 * The response can be in either plain text or JSON format.
 * </p>
 */
public enum ResponseFormat {

    /**
     * The response is in plain text format.
     */
    TEXT,

    /**
     * The response is in JSON format.
     */
    JSON
}
