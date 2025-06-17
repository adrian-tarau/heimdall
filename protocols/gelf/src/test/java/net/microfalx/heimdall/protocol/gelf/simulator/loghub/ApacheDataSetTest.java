package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApacheDataSetTest {

    private ApacheDataSet dataSet;

    @BeforeEach
    void setUp() {
        dataSet = new ApacheDataSet.Factory().createDataSet();
    }

    @Test
    void update() {
        GelfEvent event1 = new GelfEvent();
        dataSet.update(event1, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.DEBUG, event1.getGelfSeverity());
        assertEquals("workerEnv.init() ok /etc/httpd/conf/workers2.properties", event1.getBodyAsString());
        assertNotNull(event1.get("correlationalId").getValue());

        GelfEvent event2 = new GelfEvent();
        dataSet.update(event2, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.ERROR, event2.getGelfSeverity());
        assertEquals("mod_jk child workerEnv in error state 6", event2.getBodyAsString());
        assertNotNull(event2.get("correlationalId").getValue());
    }
}