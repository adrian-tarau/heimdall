package net.microfalx.heimdall.protocol.core;

import java.time.ZonedDateTime;
import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public abstract class AbstractEvent implements Event {

    private String id = UUID.randomUUID().toString();
    private String name;
    private Address source;
    private Collection<Address> targets = new ArrayList<>();
    private final Type type;
    private Severity severity = Severity.INFO;
    private ZonedDateTime receivedAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime sentAt;
    private Map<String,Object> attributes;

    private Collection<Part> parts = new ArrayList<>();

    public AbstractEvent(Type type) {
        requireNonNull(type);
        this.type = type;
    }

    public AbstractEvent(Type type, String id) {
        requireNonNull(type);
        requireNonNull(id);
        this.id = id;
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
    public final String getName() {
        return name;
    }

    public void setName(String name) {
        requireNonNull(name);
        this.name = name;
    }

    @Override
    public final Type getType() {
        return type;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    protected void setSeverity(Severity severity) {
        requireNonNull(severity);
        this.severity = severity;
    }

    @Override
    public Address getSource() {
        return source;
    }

    /**
     * Changes the source of the event.
     *
     * @param source the source
     */
    public void setSource(Address source) {
        requireNonNull(source);
        this.source = source;
    }

    /**
     * Returns one or more target addresses for an event.
     *
     * @return a non-null instance
     */
    @Override
    public Collection<Address> getTargets() {
        return Collections.unmodifiableCollection(targets);
    }

    /**
     * Adds a target to this event.
     *
     * @param address the address
     */
    public void addTarget(Address address) {
        requireNonNull(address);
        targets.add(address);
    }

    @Override
    public Body getBody() {
        for (Part part : parts) {
            if (part.getType() == Part.Type.BODY) {
                return (Body) part;
            }
        }
        return null;
    }

    public void setBody(Body body) {
        requireNonNull(body);
        Body prevBody = null;
        for (Part part : parts) {
            if (part.getType() == Part.Type.BODY) {
                prevBody = (Body) part;
                break;
            }
        }
        if (prevBody != null) this.parts.remove(prevBody);
        this.addPart(body);
    }

    @Override
    public ZonedDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(ZonedDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public ZonedDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(ZonedDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public Collection<Part> getParts() {
        return Collections.unmodifiableCollection(parts);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", source=" + source +
                ", targets=" + targets +
                ", type=" + type +
                ", receivedAt=" + receivedAt +
                ", createdAt=" + createdAt +
                ", sentAt=" + sentAt +
                ", parts=" + parts +
                '}';
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

    /**
     * Returns a collection of attributes
     *
     * @return a non-null instance
     */
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void addAttribute(String key,Object value){
        requireNonNull(key);
        requireNonNull(value);
        attributes.put(key, value);
    }
}
