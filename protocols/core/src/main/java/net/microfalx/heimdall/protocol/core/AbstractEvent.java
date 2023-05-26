package net.microfalx.heimdall.protocol.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static net.microfalx.resource.ResourceUtils.requireNonNull;

public abstract class AbstractEvent implements Event {

    private String id = UUID.randomUUID().toString();
    private String name;
    private final Type type;
    private Body body;

    private Collection<Part> parts = new ArrayList<>();

    public AbstractEvent(Type type) {
        requireNonNull(type);
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    protected void setName(String name) {
        requireNonNull(name);
        this.name = name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        requireNonNull(type);
        this.body = body;
    }

    @Override
    public Collection<Part> getParts() {
        return Collections.unmodifiableCollection(parts);
    }

    /**
     * Adds a part to the event.
     *
     * @param part the part
     */
    public void addPart(Part part) {
        requireNonNull(part);
        parts.add(part);
    }
}
