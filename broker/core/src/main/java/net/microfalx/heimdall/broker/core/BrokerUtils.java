package net.microfalx.heimdall.broker.core;

import net.microfalx.lang.UriUtils;

import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Various broker utilities
 */
public class BrokerUtils {

    /**
     * Returns the URI to the resource holding the event.
     *
     * @param sessionUri the URI of the session
     * @param eventId    the event identifier
     * @return the URI
     */
    public static URI getEventUri(String sessionUri, String eventId) {
        requireNonNull(sessionUri);
        requireNonNull(eventId);
        return UriUtils.appendFragment(sessionUri, toIdentifier(eventId));
    }
}
