package net.microfalx.heimdall.protocol.core;

import net.microfalx.resource.NullResource;
import net.microfalx.resource.Resource;

import java.util.UUID;

import static net.microfalx.resource.ResourceUtils.requireNonNull;

public abstract class AbstractPart implements Part {

    private AbstractEvent event;
    private String id = UUID.randomUUID().toString();
    private String name;
    private Type type;
    private String contentType = "application/octet-stream";
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
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        requireNonNull(contentType);
        this.contentType = contentType;
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

    public void setResource(Resource resource) {
        requireNonNull(resource);
        this.resource = resource;
    }
}
