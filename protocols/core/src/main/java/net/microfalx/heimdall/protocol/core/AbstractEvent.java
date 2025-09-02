package net.microfalx.heimdall.protocol.core;

import io.azam.ulidj.ULID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.model.AbstractAttributes;
import net.microfalx.bootstrap.model.Attribute;
import net.microfalx.resource.Resource;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Getter
@Setter
@ToString
public abstract class AbstractEvent extends AbstractAttributes<Attribute> implements Event {

    private String id = ULID.random();
    private String name;
    private Address source;
    private final Collection<Address> targets = new ArrayList<>();
    private final Type type;
    private Severity severity = Severity.INFO;
    private ZonedDateTime receivedAt;
    private ZonedDateTime createdAt = ZonedDateTime.now();
    private ZonedDateTime sentAt;

    private Resource resource;
    private final Collection<Part> parts = new ArrayList<>();

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

    protected void setId(String id) {
        requireNonNull(id);
        this.id = id;
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
        return unmodifiableCollection(targets);
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
    public final Optional<Resource> getResource() {
        return Optional.ofNullable(resource);
    }

    /**
     * Attaches the content of the event, when available
     *
     * @param resource the resource holding the event content
     */
    public void setResource(Resource resource) {
        requireNonNull(resource);
        this.resource = resource;
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
    public String getBodyAsString() {
        Body body = getBody();
        try {
            return body != null ? body.getResource().loadAsString() : null;
        } catch (IOException e) {
            throw new ProtocolException("Body cannot be extracted for " + toString(), e);
        }
    }

    @Override
    public boolean hasBody() {
        return getBody() != null;
    }

    @Override
    public boolean hasAttachments() {
        for (Part part : parts) {
            if (part instanceof Attachment) return true;
        }
        return false;
    }

    @Override
    public Collection<Part> getParts() {
        return unmodifiableCollection(parts);
    }

    /**
     * Adds a part to the event.
     *
     * @param part the part
     */
    public void addPart(Part part) {
        requireNonNull(part);
        parts.add(part);
        if (part instanceof AbstractPart apart) apart.setEvent(this);
    }

    @Override
    protected Attribute createAttribute(String name, Object value) {
        return Attribute.create(name, value);
    }
}
