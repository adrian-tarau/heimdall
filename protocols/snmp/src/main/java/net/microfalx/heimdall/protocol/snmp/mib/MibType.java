package net.microfalx.heimdall.protocol.snmp.mib;

/**
 * An enum use to separate internal and external MIBs.
 */
public enum MibType {

    /**
     * An internal MIB provided by the application.
     */
    SYSTEM,

    /**
     * A MIB which is imported from external sources on demand.
     */
    IMPORT,

    /**
     * An external Mib that the user can create.
     */
    USER;

}
