package net.microfalx.heimdall.llm.api;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * An interface for representing a chat session with an AI model.
 */
public interface Chat extends Identifiable<String>, Nameable {

    /**
     * Returns the model used by this chat session.
     *
     * @return a non-null instance
     */
    Model getModel();

    /**
     * Returns the user that created the chat.
     *
     * @return a non-null instance
     */
    Principal getUser();

    /**
     * Returns the start time of chat.
     *
     * @return a non-null instance
     */
    LocalDateTime getStartAt();

    /**
     * Returns the finish time of chat.
     *
     * @return a non-null instance
     */
    LocalDateTime getFinishAt();

    /**
     * Returns duration of the chat.
     *
     * @return a non-null instance
     */
    Duration getDuration();

    /**
     * Returns the content of the chat.
     *
     * @return a non-null instance
     */
    String getContent();

    /**
     * Returns the token count of the chat.
     *
     * @return a non-null instance
     */
    int getTokenCount();

    /**
     * Asks a question to the AI model and returns the answer.
     *
     * @param message the message to send to the model
     * @return the response as a
     */
    String ask(String message);

    /**
     * Asks a question to the AI model and returns a stream of tokens.
     *
     * @param message the message to send to the model
     * @return a stream of tokens
     */
    Iterator<String> chat(String message);

    /**
     * Completes the chat session and records the chat in the history.
     */
    void close();

    /**
     * A factory for creating chat sessions.
     */
    interface Factory {

        /**
         * Creates a chat session with a given identifier.
         *
         * @param model the model to use
         * @return a non-null instance
         */
        Chat createChat(Model model);
    }
}
