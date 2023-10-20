package net.microfalx.heimdall.protocol.core;

import net.microfalx.resource.MimeType;
import net.microfalx.resource.NullResource;
import net.microfalx.resource.Resource;

import java.io.IOException;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.NA_STRING;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;

public abstract class AbstractPart implements Part {

    private AbstractEvent event;
    private String name;
    private Type type;
    private String mimeType = MimeType.APPLICATION_OCTET_STREAM.toString();
    private String fileName;
    Resource resource = NullResource.createNull();

    public AbstractPart(Type type) {
        requireNonNull(type);
        this.type = type;
    }

    @Override
    public final Event getEvent() {
        if (event == null) throw new ProtocolException("Part not attached to event " + getDescription());
        return event;
    }

    final void setEvent(AbstractEvent event) {
        requireNonNull(event);
        this.event = event;
    }

    @Override
    public final String getName() {
        return defaultIfEmpty(name, defaultIfEmpty(fileName, NA_STRING));
    }

    protected final Part setName(String name) {
        requireNonNull(name);
        this.name = name;
        return this;
    }

    @Override
    public String getDescription() {
        return getName() + ", type " + type + ", resource " + resource.toURI();
    }

    @Override
    public final Type getType() {
        return type;
    }

    @Override
    public final String getMimeType() {
        return mimeType;
    }

    /**
     * Changes the mime type  associated with this part.
     *
     * @param mimeType the mime type
     * @return self
     */
    public final Part setMimeType(String mimeType) {
        requireNonNull(mimeType);
        this.mimeType = mimeType;
        if (this.resource != null) resource = resource.withMimeType(mimeType);
        return this;
    }

    /**
     * Changes the mime type associated with this part.
     *
     * @param mimeType the mime type
     * @return self
     */
    public final Part setMimeType(MimeType mimeType) {
        requireNonNull(mimeType);
        this.mimeType = mimeType.getValue();
        return this;
    }

    @Override
    public final String getFileName() {
        return fileName;
    }

    /**
     * Changes the file name associated with this part.
     *
     * @param fileName the file name
     * @return self
     */
    public final Part setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public final Resource getResource() {
        return resource;
    }

    @Override
    public final String loadAsString() {
        try {
            return getResource().loadAsString();
        } catch (IOException e) {
            throw new ProtocolException("Body cannot be extracted for " + event.toString(), e);
        }
    }

    /**
     * Changes the resource associated with the part.
     *
     * @param resource the resource
     * @return self
     */
    public final Part setResource(Resource resource) {
        requireNonNull(resource);
        this.resource = resource;
        setMimeType(resource.getMimeType());
        return this;
    }

    @Override
    public String toString() {
        return "AbstractPart{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", mimeType='" + mimeType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", resource=" + resource +
                ", event=" + (event != null ? event.getName() : null) +
                '}';
    }
}
