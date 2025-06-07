package net.microfalx.heimdall.llm.api;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Represents a tool that can be used in the context of an LLM (Large Language Model) API.
 * <p>
 * A tool can have various parameters that define its behavior or configuration.
 * </p>
 */
public class Tool extends NamedAndTaggedIdentifyAware<String> {

    private Map<String, Object> parameters;

    /**
     * Returns the parameters of the tool.
     *
     * @return an unmodifiable map of parameters
     */
    public Map<String, Object> getParameters() {
        return unmodifiableMap(parameters);
    }

    /**
     * An interface representing a request to execute a tool.
     */
    public interface ExecutionRequest extends Identifiable<String>, Nameable {

        /**
         * Returns the arguments of the tool.
         *
         * @return a non-null string representing the arguments
         */
        String getArguments();
    }

    /**
     * Builder for {@link Tool}.
     */
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private Map<String, Object> parameters = new HashMap<>();

        /**
         * Adds a parameter to the tool.
         *
         * @param key   the parameter key
         * @param value the parameter value
         * @return this builder instance
         */
        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        /**
         * Adds multiple parameters to the tool.
         *
         * @param parameters the parameters map
         * @return this builder instance
         */
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        @Override
        protected IdentityAware create() {
            return new Tool();
        }

        @Override
        public NamedAndTaggedIdentifyAware<String> build() {
            Tool tool = (Tool) super.build();
            tool.parameters = parameters;
            return tool;
        }
    }

}
