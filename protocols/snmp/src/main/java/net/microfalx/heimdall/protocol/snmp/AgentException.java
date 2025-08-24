package net.microfalx.heimdall.protocol.snmp;

/**
 * An exception which occurs in the SNMP agent.
 */
public class AgentException extends SnmpException {

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
