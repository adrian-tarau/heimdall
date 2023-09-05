package net.microfalx.heimdall.protocol.snmp.mib;

import net.microfalx.resource.Resource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MibFetcherTest {

    @Test
    void testRFC1212() throws IOException {
        Resource resource = MibFetcher.create("RFC-1212").execute();
        assertTrue(resource.exists());
        assertTrue(resource.length() > 100);
    }

    @Test
    void testAPEX() throws IOException {
        Resource resource = MibFetcher.create("APEX-MIB").execute();
        assertTrue(resource.exists());
        assertTrue(resource.length() > 100);
    }

}