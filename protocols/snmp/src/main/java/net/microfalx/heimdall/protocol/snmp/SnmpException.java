package net.microfalx.heimdall.protocol.snmp;

import net.microfalx.heimdall.protocol.core.ProtocolException;

/**
 * Base exception for SNMP protocol errors.
 */
public class SnmpException extends ProtocolException {

    public SnmpException(String message) {
        super(message);
    }

    public SnmpException(String message, Throwable cause) {
        super(message, cause);
    }
}
