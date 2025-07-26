package net.microfalx.heimdall.llm.api;

import net.microfalx.lang.Identifiable;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * An interface representing a message in the context of a chat session with an LLM model.
 */
public interface Message extends Identifiable<String> {

    /**
     * The type of the message.
     *
     * @return the type of the message
     */
    Type getType();

    /**
     * The {@link Content}s of the message.
     *
     * @return the contents.
     */
    List<Content> getContent();

    /**
     * Returns text from the content associated with the message.
     *
     * @return the text
     */
    String getText();

    /**
     * Returns the timestamp of when the message was created.
     *
     * @return a non-null instance
     */
    ZonedDateTime getTimestamp();

    /**
     * An enumeration representing the type of message in a chat session.
     */
    enum Type {

        /**
         * A message that is sent by the user.
         */
        USER,

        /**
         * A message that is sent by the AI model.
         */
        MODEL,

        /**
         * A system message that provides context or instructions.
         */
        SYSTEM,

        /**
         * A message that provides context or instructions.
         */
        TOOL,

        /**
         * A custom message type that does not fit into the standard categories.
         */
        CUSTOM
    }
}
