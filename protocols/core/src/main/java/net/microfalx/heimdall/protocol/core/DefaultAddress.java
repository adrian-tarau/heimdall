package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.StringUtils;

import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class DefaultAddress implements Address {

    private final Type type;
    private final String name;
    private final String value;

    DefaultAddress(Type type, String name, String value) {
        requireNonNull(type);
        requireNonNull(value);
        this.type = type;
        this.name = StringUtils.defaultIfEmpty(name, value);
        this.value = value;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultAddress that = (DefaultAddress) o;
        return type == that.type && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "DefaultAddress{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
