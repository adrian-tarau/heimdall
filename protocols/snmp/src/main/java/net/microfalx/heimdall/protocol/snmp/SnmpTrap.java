package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.AbstractEvent;

public class SnmpTrap extends AbstractEvent {

    public SnmpTrap() {
        super(Type.SNMP);
    }
}
