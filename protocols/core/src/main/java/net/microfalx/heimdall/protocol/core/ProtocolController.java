package net.microfalx.heimdall.protocol.core;

import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.protocol.core.jpa.Event;

/**
 * Base class for all protocol controllers.
 *
 * @param <E> the event type
 */
public abstract class ProtocolController<E extends Event> extends DataSetController<E, Integer> {
}
