package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.StringUtils;

import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;

public class DefaultAddress implements Address {

    private final Type type;
    private final String name;
    private final String value;

    DefaultAddress(Type type, String value, String name) {
        requireNonNull(type);
        requireNonNull(value);
        this.type = type;
        if (type == Type.HOSTNAME) {
            value = StringUtils.removeStartSlash(value);
            if (isLocalHost(value) && isEmpty(name)) name = "Local";
        }
        this.value = value;
        this.name = getNameFromValue(type, value, name);

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
    public String toDisplay() {
        return value.equals(name) ? name : name + " " + value;
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

    private boolean isLocalHost(String value) {
        return "127.0.0.1".equals(value) || "localhost".equalsIgnoreCase(value) || "::1".equals(value);
    }

    private String getNameFromValue(Type type, String value, String name) {
        if (StringUtils.isNotEmpty(name)) return name;
        if (type == Type.EMAIL) {
            int index = value.lastIndexOf('@');
            if (index == -1) return value;
            String prefix = value.substring(0, index);
            // if it has a "." and it looks capitalized, it is probably a name
            if (prefix.contains(".") && Character.isUpperCase(prefix.charAt(0))) {
                return String.join(" ", StringUtils.split(prefix, ".", true));
            }
        }
        return value;
    }
}
