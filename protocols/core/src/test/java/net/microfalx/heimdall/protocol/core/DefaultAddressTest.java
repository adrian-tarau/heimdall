package net.microfalx.heimdall.protocol.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAddressTest {

    @Test
    void email() {
        assertEquals(Address.Type.EMAIL, Address.email("john@company.com").getType());
        assertEquals("john@company.com",Address.email("john@company.com").getValue());
        assertEquals("john@company.com",Address.email("john@company.com").getName());

        assertEquals(Address.Type.EMAIL, Address.email("john.doe@company.com").getType());
        assertEquals("john.doe@company.com",Address.email("john.doe@company.com").getValue());
        assertEquals("john.doe@company.com",Address.email("john.doe@company.com").getName());

        assertEquals(Address.Type.EMAIL, Address.email("John.Doe@company.com").getType());
        assertEquals("John.Doe@company.com",Address.email("John.Doe@company.com").getValue());
        assertEquals("John Doe",Address.email("John.Doe@company.com").getName());
    }

    @Test
    void host() {
        assertEquals(Address.Type.HOSTNAME, Address.host("localhost").getType());
        assertEquals("localhost",Address.host("localhost").getValue());
        assertEquals("localhost",Address.host("localhost").getName());

        assertEquals(Address.Type.HOSTNAME, Address.host("localhost", "Local").getType());
        assertEquals("localhost",Address.host("localhost", "Local").getValue());
        assertEquals("Local",Address.host("localhost", "Local").getName());
    }

}