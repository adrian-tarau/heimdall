package net.microfalx.heimdall.protocol.snmp.mib;

/**
 * An enum use to separate internal and external Mibs
 */
public enum MibType {

    /**
     * An internal Mib provided by the application
     */
    SYSTEM,

    /**
     * An external Mib that the user can create
     */
    USER;

}
