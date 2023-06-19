package net.microfalx.heimdall.protocol.core;

import net.microfalx.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum MimeType {

    TEXT_PLAIN("text/plain"),
    TEXT_CSS("text/css"),
    TEXT_JAVASCRIPT("text/javascript"),
    TEXT_HTML("text/html"),
    TEXT_XML("text/xml"),
    TEXT("text/*"),
    IMAGE_PNG("image/png"),
    IMAGE_BMP("image/bmp"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_GIF("image/gif"),
    IMAGE_TIFF("image/tiff"),
    IMAGE("image/*"),
    FONT("font/*"),
    APPLICATION_JSON("application/json"),
    APPLICATION_OCTET_STREAM("application/octet-stream");

    private String value;

    /**
     * Returns a mime type enum based on its string value.
     * <p>
     * If it cannot be resolved, it will return {@link #APPLICATION_OCTET_STREAM}.
     *
     * @param value the mime type (content type)
     * @return a non-null instance
     */
    public static MimeType get(String value) {
        if (StringUtils.isEmpty(value)) return APPLICATION_OCTET_STREAM;
        String[] parts = StringUtils.split(value, ";");
        MimeType mimeType = cache.get(parts[0].toLowerCase());
        return mimeType != null ? mimeType : APPLICATION_OCTET_STREAM;
    }

    MimeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "MimeType{" +
                "value='" + value + '\'' +
                "} " + super.toString();
    }

    private static final Map<String, MimeType> cache = new HashMap<>();

    static {
        for (MimeType mimeType : MimeType.values()) {
            cache.put(mimeType.name().toLowerCase(), mimeType);
            cache.put(mimeType.getValue().toLowerCase(), mimeType);
        }
    }
}
