package net.microfalx.heimdall.protocol.snmp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.protocol.core.AbstractEvent;
import net.microfalx.heimdall.protocol.core.Severity;
import net.microfalx.lang.EnumUtils;
import org.snmp4j.PDU;

@Getter
@Setter
@ToString(callSuper = true)
public class SnmpEvent extends AbstractEvent {

    private int version;
    private String community;
    private String enterprise;
    private int pduType = PDU.TRAP;
    private int maxRepetitions = 10;

    public SnmpEvent() {
        super(Type.SNMP);
    }

    public void setSeverity(String value) {
        setSeverity(EnumUtils.fromName(Severity.class, value, Severity.INFO));
    }
}
