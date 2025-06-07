package net.microfalx.heimdall.llm.api;

import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;

/**
 * Represents a piece of content that can be used in a chat session with an LLM model.
 * <p>
 * Content can be of various types such as text, image, audio, video, or document.
 * </p>
 */
public interface Content extends Nameable {

    /**
     * Returns the type of the content.
     *
     * @return a non-null instance of {@link Type} representing the content type
     */
    Type getType();

    /**
     * Returns the resource associated with the content.
     *
     * @return a non-null instance of {@link Resource} representing the content resource
     */
    Resource getResource();

    /**
     * An enumeration representing the type of content.
     */
    enum Type {

        /**
         * Text content.
         */
        TEXT,

        /**
         * Image content.
         */
        IMAGE,

        /**
         * Audio content.
         */
        AUDIO,

        /**
         * Video content.
         */
        VIDEO,

        /**
         * Document content.
         */
        DOCUMENT
    }
}
