package net.microfalx.heimdall.infrastructure.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceTest {

    @Test
    void createCommon() {
        assertEquals(Service.Type.HTTP, Service.create(Service.Type.HTTP).getType());
        assertEquals("The home page of the web server over HTTP", Service.create(Service.Type.HTTP).getDescription());
        assertEquals(22, Service.create(Service.Type.SSH).getPort());
    }

}