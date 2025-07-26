package net.microfalx.heimdall.llm.api;

import lombok.ToString;
import net.microfalx.lang.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Represents a tool that can be used in the context of an LLM (Large Language Model) API.
 * <p>
 * A tool can have various parameters that define its behavior or configuration.
 * </p>
 */
@ToString(callSuper = true)
public class Tool extends NamedAndTaggedIdentifyAware<String> {

    private Map<String, Parameter> parameters;
    private Executor executor;

    /**
     * Creates a new instance of a builder for a tool with the specified name.
     *
     * @param name the tool name
     * @return the builder instance
     */
    public static Builder builder(String name) {
        return (Builder) new Builder().name(name);
    }

    /**
     * Returns the parameters of the tool.
     *
     * @return a non-null instance
     */
    public Map<String, Parameter> getParameters() {
        return unmodifiableMap(parameters);
    }

    /**
     * Returns the handler that executes the tool.
     *
     * @return a non-null instance
     */
    public Executor getExecutor() {
        return executor;
    }

    /**
     * An interface representing a request to execute a tool.
     */
    public interface ExecutionRequest extends Identifiable<String>, Nameable {

        /**
         * Returns the tool to be executed.
         *
         * @return a non-null instance
         */
        Tool getTool();

        /**
         * Returns the arguments of the tool.
         *
         * @return a non-null
         */
        Map<String, Object> getArguments();
    }

    public interface Executor {

        /**
         * Executes the tool with the given request.
         *
         * @param request the execution request
         * @return a non-null object representing the result of the execution (usually a text or JSON response)
         */
        Object execute(ExecutionRequest request);
    }

    @ToString(callSuper = true)
    public static class Parameter extends NamedIdentityAware<String> {

        private Type type = Type.STRING;
        private boolean required;

        /**
         * Creates a new instance of a builder for a tool with the specified name.
         *
         * @param name the tool name
         * @return the builder instance
         */
        public static Parameter.Builder builder(String name) {
            return (Parameter.Builder) new Parameter.Builder().name(name);
        }

        Parameter() {
        }

        Parameter(String name, Type type, boolean required) {
            setName(name);
            this.type = type;
            this.required = required;
        }

        /**
         * Returns the (data) type of the parameter.
         *
         * @return a non-null instance
         */
        public Type getType() {
            return type;
        }

        /**
         * Returns whether the parameter is required.
         *
         * @return {@code true} if the parameter is required, {@code false} otherwise
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Builder for {@link Tool}.
         */
        public static class Builder extends NamedIdentityAware.Builder<String> {

            private Type type = Type.STRING;
            private boolean required;

            public Builder type(Type type) {
                this.type = type;
                return this;
            }

            public Builder required(boolean required) {
                this.required = required;
                return this;
            }

            @Override
            protected IdentityAware<String> create() {
                return new Parameter();
            }

            @Override
            protected String updateId() {
                return StringUtils.toIdentifier(name());
            }

            @Override
            public Parameter build() {
                Parameter parameter = (Parameter) super.build();
                parameter.type = type;
                parameter.required = required;
                return parameter;
            }
        }

        /**
         * An enumeration representing the (data) type of a parameter.
         */
        public enum Type {
            STRING,
            INTEGER,
            DECIMAL,
            BOOLEAN,
            DATE,
            DATE_TIME,
            ARRAY;

            public static Type fromObject(Object value) {
                if (value == null) return STRING;
                if (value instanceof Integer || value instanceof Long) {
                    return INTEGER;
                } else if (value instanceof Float || value instanceof Double) {
                    return DECIMAL;
                } else if (value instanceof LocalDateTime || value instanceof ZonedDateTime) {
                    return DATE_TIME;
                } else if (value instanceof LocalDate) {
                    return DATE;
                } else {
                    return STRING;
                }
            }
        }
    }

    /**
     * Builder for {@link Tool}.
     */
    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private final Map<String, Parameter> parameters = new LinkedHashMap<>();
        private final Map<String, Object> values = new HashMap<>();
        private Executor executor;

        /**
         * Registers a parameter with the tool.
         *
         * @param parameter the parameter to register
         * @return self
         */
        public Builder parameter(Parameter parameter) {
            requireNonNull(parameter);
            parameters.put(parameter.getName(), parameter);
            return this;
        }

        /**
         * Registers a parameter with the tool.
         *
         * @param name        the name of the parameter
         * @param description the description of the parameter
         * @return self
         */
        public Builder parameter(String name, String description) {
            requireNonNull(name);
            requireNonNull(description);
            return parameter((Parameter) new Parameter.Builder().name(name).description(description).build());
        }

        /**
         * Sets the executor for this tool.
         *
         * @param executor the executor to set, must not be null
         * @return self
         */
        public Builder executor(Executor executor) {
            requireNonNull(executor);
            this.executor = executor;
            return this;
        }

        @Override
        protected String updateId() {
            return StringUtils.toIdentifier(name());
        }

        @Override
        protected IdentityAware<String> create() {
            return new Tool();
        }

        @Override
        public NamedAndTaggedIdentifyAware<String> build() {
            if (executor == null) throw new IllegalArgumentException("Executor is required");
            Tool tool = (Tool) super.build();
            tool.parameters = parameters;
            tool.executor = executor;
            return tool;
        }
    }

}
