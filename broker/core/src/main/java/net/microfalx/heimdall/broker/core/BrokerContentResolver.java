package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentLocator;
import net.microfalx.bootstrap.content.ContentResolver;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceFactory;
import net.microfalx.resource.archive.ArchiveResource;

import java.io.IOException;

@Provider
public class BrokerContentResolver implements ContentResolver {

    @Override
    public Content resolve(ContentLocator locator) throws IOException {
        Resource resource = ResourceFactory.resolve(locator.getUri());
        if (StringUtils.isNotEmpty(resource.getFragment())) {
            resource = ArchiveResource.create(resource);
            Resource eventResource;
            try {
                BrokerTopicSnapshot.Event event = BrokerTopicSnapshot.Event.deserialize(resource.getInputStream());
                eventResource = MemoryResource.create(event.getValue()).copyPropertiesFrom(resource);
                eventResource = eventResource.withMimeType(eventResource.detectMimeType());
            } catch (Exception e) {
                eventResource = MemoryResource.create("#Error: " + ExceptionUtils.getRootCauseMessage(e));
            }
            return Content.create(eventResource);
        }
        return Content.create(locator, resource);
    }

    @Override
    public Content intercept(Content content) throws IOException {
        return ContentResolver.super.intercept(content);
    }

    @Override
    public boolean supports(ContentLocator locator) {
        return locator.getUri().toASCIIString().startsWith("shared:/broker/session");
    }
}
