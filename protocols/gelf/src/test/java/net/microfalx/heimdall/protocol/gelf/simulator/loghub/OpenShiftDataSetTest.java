package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenShiftDataSetTest {

    private OpenShiftDataSet dataSet;

    @BeforeEach
    void setUp() {
        dataSet = new OpenShiftDataSet.Factory().createDataSet();
    }

    @Test
    void update() {
        GelfEvent event1 = new GelfEvent();
        dataSet.update(event1, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event1.getGelfSeverity());
        assertEquals("nova.osapi_compute.wsgi.server", event1.getLogger());
        assertEquals("10.11.10.1 \"GET /v2/54fadb412c4e40cdbaed9335e4c35a9e/servers/detail HTTP/1.1\" status: 200 len: 1893 time: 0.2477829", event1.getBodyAsString());
        assertEquals("25746", event1.get("pid").asString());

        GelfEvent event2 = new GelfEvent();
        dataSet.update(event2, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event2.getGelfSeverity());
        assertEquals("nova.osapi_compute.wsgi.server", event2.getLogger());
        assertEquals("10.11.10.1 \"GET /v2/54fadb412c4e40cdbaed9335e4c35a9e/servers/detail HTTP/1.1\" status: 200 len: 1893 time: 0.2577181", event2.getBodyAsString());
        assertEquals("25746", event2.get("pid").asString());
    }
}