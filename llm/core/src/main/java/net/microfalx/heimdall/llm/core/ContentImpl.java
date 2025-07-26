package net.microfalx.heimdall.llm.core;

import dev.langchain4j.data.audio.Audio;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.pdf.PdfFile;
import lombok.ToString;
import net.microfalx.heimdall.llm.api.Content;
import net.microfalx.lang.UriUtils;
import net.microfalx.resource.Resource;

import java.net.URL;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.emptyIfNull;

@ToString
public class ContentImpl implements Content {

    private final Type type;
    private final Resource resource;

    static Content from(dev.langchain4j.data.message.Content content) {
        requireNonNull(content);
        return new ContentImpl(getType(content), getResource(content));
    }

    static Content from(String text) {
        text = emptyIfNull(text);
        return new ContentImpl(Content.Type.TEXT, Resource.text(text));
    }

    private ContentImpl(Type type, Resource resource) {
        requireNonNull(type);
        requireNonNull(resource);
        this.type = type;
        this.resource = resource;
    }

    @Override
    public String getName() {
        return resource.getName();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    private static Type getType(dev.langchain4j.data.message.Content content) {
        return switch (content.type()) {
            case TEXT -> Type.TEXT;
            case IMAGE -> Type.IMAGE;
            case AUDIO -> Type.AUDIO;
            case VIDEO -> Type.VIDEO;
            case PDF -> Type.DOCUMENT;
        };
    }

    private static Resource getResource(dev.langchain4j.data.message.Content content) {
        if (content instanceof AudioContent audioContent) {
            Audio audio = audioContent.audio();
            return create(UriUtils.toUrl(audio.url()), audio.base64Data());
        } else if (content instanceof TextContent textContent) {
            return Resource.text(textContent.text());
        } else if (content instanceof PdfFileContent pdfFileContent) {
            PdfFile pdfFile = pdfFileContent.pdfFile();
            return create(UriUtils.toUrl(pdfFile.url()), pdfFile.base64Data());
        } else {
            throw new IllegalArgumentException("Unsupported content type: " + content.type());
        }
    }

    private static Resource create(URL url, String base64Encoded) {
        if (url != null) {
            return Resource.url(url);
        } else if (base64Encoded != null) {
            return Resource.base64Encoded(base64Encoded);
        } else {
            throw new IllegalArgumentException("Either URL or base64Encoded must be provided");
        }
    }
}
