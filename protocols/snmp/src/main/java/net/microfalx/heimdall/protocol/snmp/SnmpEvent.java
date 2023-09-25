package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.AbstractEvent;
import net.microfalx.heimdall.protocol.core.Severity;
import net.microfalx.lang.EnumUtils;

public class SnmpEvent extends AbstractEvent {

    private int version;
    private String community;
    private String enterprise;

    public SnmpEvent() {
        super(Type.SNMP);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    void setSeverity(String value) {
        setSeverity(EnumUtils.fromName(Severity.class, value, Severity.INFO));
    }
}
