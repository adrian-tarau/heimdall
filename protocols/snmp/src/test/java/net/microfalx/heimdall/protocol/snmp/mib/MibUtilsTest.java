package net.microfalx.heimdall.protocol.snmp.mib;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MibUtilsTest {

    @Test
    void isOid() {
        assertFalse(MibUtils.isOid(null));
        assertFalse(MibUtils.isOid(""));
        assertFalse(MibUtils.isOid("a"));
        assertFalse(MibUtils.isOid("a"));
        assertFalse(MibUtils.isOid("a.1"));
        assertTrue(MibUtils.isOid("0"));
        assertTrue(MibUtils.isOid("0.1"));
        assertTrue(MibUtils.isOid("1.12.3"));
    }

}