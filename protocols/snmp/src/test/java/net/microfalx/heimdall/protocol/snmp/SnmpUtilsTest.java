package net.microfalx.heimdall.protocol.snmp;

import org.junit.jupiter.api.Test;
import org.snmp4j.smi.OID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnmpUtilsTest {

    @Test
    void getBaseOid() {
        assertEquals("", new OID("1.3.6.1.4.1.2281.10.7.8.5.10.0").toDottedString());
        assertEquals("", new OID("1.3.6.1.4.1.2281.10.6.3.11.1.26.268460097").toDottedString());
        assertEquals("", new OID("1.3.6.1.4.1.2281.10.7.4.3.1.1.268451905.5760").toDottedString());
    }

}