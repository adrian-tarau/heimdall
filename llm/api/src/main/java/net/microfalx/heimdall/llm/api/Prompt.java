package net.microfalx.heimdall.llm.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.StringUtils;

/**
 * Represents a prompt in the LLM (Large Language Model) API.
 */
@ToString(callSuper = true)
public class Prompt extends NamedAndTaggedIdentifyAware<String> {

    private String role;
    private Integer maximumInputEvents;
    private Integer maximumOutputTokens;
    private boolean chainOfThought;
    private boolean useOnlyContext;
    private String examples;
    private String context;
    private String question;

    /**
     * Creates an empty prompt.
     * <p>
     * An empty prompt has no role, examples, context, or question.
     * </p>
     *
     * @return a new instance of {@link Prompt} with no content
     */
    public static Prompt empty() {
        return (Prompt) new Builder().id("empty").build();
    }

    /**
     * Creates a prompt with the specified identifier and name.
     * <p>
     *
     * @param id   the unique identifier for the prompt
     * @param name the name of the prompt
     * @return a new instance of {@link Prompt} with the specified id and name
     */

    public static Builder create(String id, String name) {
        return (Builder) new Builder().name(name).id(id);
    }

    /**
     * Returns the role of the prompt.
     * <p>
     * The role is used to define the context or purpose of the prompt in the chat completion.
     *
     * @return a non-null string representing the role, null if not set
     */
    public String getRole() {
        return role;
    }

    /**
     * Returns the examples associated with the prompt.
     *
     * @return a string containing examples, or null if not set
     */
    public String getExamples() {
        return examples;
    }

    /**
     * Returns the context associated with the prompt.
     * <p>
     * The text withing the context supports variable substitution, which allows users to
     * include dynamic content:
     * <ul>
     *     <li>{{SCHEMA}} - will be replaced with data extracted from the current context (dashboard)</li>
     *     <li>{{DATASET}} - will be replaced with data extracted from the current context (dashboard)</li>
     * </ul>
     *
     * @return a string containing context, or null if not set
     */
    public String getContext() {
        return context;
    }

    /**
     * Returns the starting question associated with the prompt.
     *
     * @return a string containing the question, or null if not set
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Returns the maximum number of input events allowed for the prompt.
     *
     * @return the maximum input events, or null if not set
     */
    public Integer getMaximumInputEvents() {
        return maximumInputEvents;
    }

    /**
     * Returns the maximum number of tokens that can be generated in the chat completion.
     *
     * @return a positive integer
     */
    public Integer getMaximumOutputTokens() {
        return maximumOutputTokens;
    }

    /**
     * Returns whether chain-of-thought prompting is enabled.
     *
     * @return {@code true} if chain-of-thought is enabled, {@code false} otherwise
     */
    public boolean isChainOfThought() {
        return chainOfThought;
    }

    /**
     * Returns whether the prompt should use only information provided in the context without reaching out
     * to tools, web searches or other means of information extraction.
     *
     * @return {@code true} if only context is used, {@code false} otherwise
     */
    public boolean isUseOnlyContext() {
        return useOnlyContext;
    }

    /**
     * Checks if the prompt is empty.
     * <p>
     * A prompt is considered empty if it has no role, examples, context, or question.
     *
     * @return {@code true} if the prompt is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return StringUtils.isEmpty(role) && StringUtils.isEmpty(examples) && StringUtils.isEmpty(context)
                && StringUtils.isEmpty(question);
    }

    /**
     * An enumeration representing the fragments of a prompt.
     */
    public enum Fragment {
        ROLE,
        EXAMPLES,
        CONTEXT,
        QUESTION
    }

    /**
     * A builder class.
     */
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private String role;
        private String examples;
        private String context;
        private String question;
        private Integer maximumInputEvents;
        private Integer maximumOutputTokens;
        private boolean chainOfThought;
        private boolean useOnlyContext = true;

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder examples(String examples) {
            this.examples = examples;
            return this;
        }

        public Builder context(String context) {
            this.context = context;
            return this;
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder maximumInputEvents(Integer maximumInputEvents) {
            this.maximumInputEvents = maximumInputEvents;
            return this;
        }

        public Builder maximumOutputTokens(Integer maximumOutputTokens) {
            this.maximumOutputTokens = maximumOutputTokens;
            return this;
        }

        public Builder chainOfThought(boolean chainOfThought) {
            this.chainOfThought = chainOfThought;
            return this;
        }

        public Builder useOnlyContext(boolean useOnlyContext) {
            this.useOnlyContext = useOnlyContext;
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Prompt();
        }

        @Override
        public Prompt build() {
            Prompt prompt = (Prompt) super.build();
            prompt.role = role;
            prompt.examples = examples;
            prompt.context = context;
            prompt.question = question;
            prompt.maximumInputEvents = maximumInputEvents;
            prompt.maximumOutputTokens = maximumOutputTokens;
            prompt.chainOfThought = chainOfThought;
            prompt.useOnlyContext = useOnlyContext;
            return prompt;
        }
    }
}
