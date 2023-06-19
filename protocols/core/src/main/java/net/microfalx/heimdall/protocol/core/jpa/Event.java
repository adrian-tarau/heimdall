package net.microfalx.heimdall.protocol.core.jpa;

import jakarta.persistence.MappedSuperclass;

/**
 * Base class for all protocol events.
 */
@MappedSuperclass
public class Event extends TimestampAware {
}
