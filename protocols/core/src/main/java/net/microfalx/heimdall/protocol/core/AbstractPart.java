package net.microfalx.heimdall.protocol.core;

import net.microfalx.resource.NullResource;
import net.microfalx.resource.Resource;
import org.apache.tika.Tika;

import java.io.IOException;
import java.util.UUID;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public abstract class AbstractPart implements Part {

    private AbstractEvent event;
    private String id = UUID.randomUUID().toString();
    private String name;
    private Type type;
    private String mimeType = MimeType.APPLICATION_OCTET_STREAM.getValue();
    private String fileName;
    private Resource resource = NullResource.createNull();

    public AbstractPart(AbstractEvent event, Type type) {
        requireNonNull(event);
        requireNonNull(type);
        this.event = event;
        this.name = event.getName();
        this.type = type;
    }

    @Override
    public final Event getEvent() {
        return event;
    }

    @Override
    public String getId() {
        return id;
    }

    protected void setId(String id) {
        requireNonNull(event);
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
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        requireNonNull(mimeType);
        this.mimeType = mimeType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public String loadAsString() {
        try {
            return getResource().loadAsString();
        } catch (IOException e) {
            throw new ProtocolException("Body cannot be extracted for " + event.toString(), e);
        }
    }

    public void setResource(Resource resource) {
        requireNonNull(resource);
        this.resource = resource;
        Tika tika = new Tika();
        try {
            setMimeType(tika.detect(resource.getInputStream()));
        } catch (IOException e) {
            // if we cannot detect, just assume is binary
        }
    }


}
