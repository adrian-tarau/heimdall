package net.microfalx.heimdall.protocol.syslog;

import net.microfalx.heimdall.protocol.core.AbstractEvent;

public class SyslogMessage extends AbstractEvent {

    public SyslogMessage() {
        super(Type.SYSLOG);
    }
}
