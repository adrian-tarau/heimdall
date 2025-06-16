package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HadoopDataSetTest {

    private HadoopDataSet dataSet;

    @BeforeEach
    void setUp() {
        dataSet = new HadoopDataSet.Factory().createDataSet();
    }

    @Test
    void update() {
        GelfEvent event1 = new GelfEvent();
        dataSet.update(event1, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event1.getGelfSeverity());
        assertEquals("org.apache.hadoop.mapreduce.v2.app.MRAppMaster", event1.getLogger());
        assertEquals("Created MRAppMaster for application appattempt_1445144423722_0020_000001", event1.getBodyAsString());
        assertEquals("main", event1.get("process").asString());

        GelfEvent event2 = new GelfEvent();
        dataSet.update(event2, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event2.getGelfSeverity());
        assertEquals("org.apache.hadoop.mapreduce.v2.app.MRAppMaster", event2.getLogger());
        assertEquals("Executing with tokens:", event2.getBodyAsString());
        assertEquals("main", event2.get("process").asString());
    }
}