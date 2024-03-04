package net.microfalx.heimdall.broker.core;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.capitalizeWords;

/**
 * A class which carries information about a broker.
 */
public class Broker implements Identifiable<String>, Nameable, Cloneable {

    private final String id;
    private final Type type;
    private String name;
    private Map<String, String> parameters = new HashMap<>();

    public Builder build(Type type, String id) {
        return new Builder(type, id);
    }

    public Broker(Type type, String id, String name) {
        requireNonNull(type);
        requireNonNull(id);
        this.type = type;
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        KAFKA,
        PULSAR,
        RABBITMQ
    }

    public static class Builder {

        private final String id;
        private final Type type;
        private String name;
        private Map<String, String> parameters = new HashMap<>();

        public Builder(Type type, String id) {
            requireNonNull(type);
            requireNonNull(id);
            this.type = type;
            this.id = id;
            this.name = capitalizeWords(id);
        }

        public Builder parameter(String name, String value) {
            requireNonNull(name);
            parameters.put(name, value);
            return this;
        }

        public Builder parameters(Map<String, ?> parameters) {
            requireNonNull(parameters);
            for (Map.Entry<String, ?> entry : parameters.entrySet()) {
                this.parameters.put(entry.getKey(), ObjectUtils.toString(entry.getValue()));
            }
            return this;
        }

        public Broker build() {
            Broker broker = new Broker(type, id, name);
            broker.parameters.putAll(parameters);
            return broker;
        }
    }
}
