package net.microfalx.heimdall.llm.api;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.io.Closeable;

/**
 * An interface for representing a chat session with an AI model.
 */
public interface Chat extends Identifiable<String>, Nameable, Closeable {

    /**
     * Completes the chat session and records the chat in the the history
     */
    @Override
    default void close() {

    }
}
