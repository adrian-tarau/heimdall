package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SparkDataSetTest {

    private SparkDataSet dataSet;

    @BeforeEach
    void setUp() {
        dataSet = new SparkDataSet.Factory().createDataSet();
    }

    @Test
    void update() {
        GelfEvent event1 = new GelfEvent();
        dataSet.update(event1, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event1.getGelfSeverity());
        assertEquals("executor.CoarseGrainedExecutorBackend", event1.getLogger());
        assertEquals("Registered signal handlers for [TERM, HUP, INT]", event1.getBodyAsString());
        assertNotNull(event1.get("correlationalId").getValue());

        GelfEvent event2 = new GelfEvent();
        dataSet.update(event2, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event2.getGelfSeverity());
        assertEquals("spark.SecurityManager", event2.getLogger());
        assertEquals("Changing view acls to: yarn,curi", event2.getBodyAsString());
        assertNotNull(event2.get("correlationalId").getValue());
    }
}