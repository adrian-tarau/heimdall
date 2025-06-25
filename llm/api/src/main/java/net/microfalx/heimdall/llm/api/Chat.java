package net.microfalx.heimdall.llm.api;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * An interface for representing a chat session with an AI model.
 */
public interface Chat extends Identifiable<String>, Nameable, Descriptable {

    /**
     * Returns the prompt used to start a chat session.
     *
     * @return a non-null instance
     */
    Prompt getPrompt();

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
     * Returns the system message that provides context or instructions for the chat session.
     * @return a non-null instance
     */
    Message getSystemMessage();

    /**
     * Returns the messages exchanged in the chat.
     *
     * @return a non-null collection of messages
     */
    Collection<Message> getMessages();

    /**
     * Returns the number of messages in the chat.
     *
     * @return a positive integer
     */
    int getMessageCount();

    /**
     * Returns the content of the chat in text form.
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
    TokenStream chat(String message);

    /**
     * Registers a feature with the chat session. Features can be used to add context to the chat session.
     * <p>
     * If the feature object is null or If the feature already exists, it will be ignored.
     *
     * @param feature the feature to add
     * @param <F>     the feature type
     */
    <F> void addFeature(F feature);

    /**
     * Returns a feature of the specified type from the chat session.
     *
     * @param featureType the type of the feature to retrieve
     * @param <F>         the feature type
     * @return the feature of the specified type, or null if not found
     */
    <F> F getFeature(Class<F> featureType);

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
         * @param prompt the prompt to use for the chat
         * @param model the model to use for the chat
         * @return a non-null instance
         */
        Chat createChat(Prompt prompt, Model model);
    }
}
