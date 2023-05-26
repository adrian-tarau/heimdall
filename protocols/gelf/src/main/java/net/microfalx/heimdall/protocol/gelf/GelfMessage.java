package net.microfalx.heimdall.protocol.gelf;

import net.microfalx.heimdall.protocol.core.AbstractEvent;

public class GelfMessage extends AbstractEvent {

    public GelfMessage() {
        super(Type.GELF);
    }
}
