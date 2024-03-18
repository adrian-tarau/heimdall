package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentListener;
import net.microfalx.bootstrap.content.ContentLocator;
import net.microfalx.lang.annotation.Provider;

import java.io.IOException;

@Provider
public class GelfContentListener implements ContentListener {

    @Override
    public Content resolve(ContentLocator locator) {
        return null;
    }

    @Override
    public void update(Content content) throws IOException {

    }

    @Override
    public boolean supports(ContentLocator locator) {
        return false;
    }
}
