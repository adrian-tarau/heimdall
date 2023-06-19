package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.AbstractEvent;

public class SnmpEvent extends AbstractEvent {

    public SnmpEvent() {
        super(Type.SNMP);
    }
}
