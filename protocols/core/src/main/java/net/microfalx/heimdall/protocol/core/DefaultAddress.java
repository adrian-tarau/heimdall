package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.StringUtils;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class DefaultAddress implements Address {

    private String name;
    private String value;

    DefaultAddress(String name, String value) {
        requireNonNull(value);
        this.name = StringUtils.defaultIfEmpty(name, value);
        this.value = value;
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
        if (!(o instanceof DefaultAddress that)) return false;

        if (!name.equals(that.name)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultAddress{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
