package net.microfalx.heimdall.protocol.gelf.simulator.loghub;

import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HDFSDataSetTest {

    private HDFSDataSet dataSet;

    @BeforeEach
    void setUp() {
        dataSet = new HDFSDataSet.Factory().createDataSet();
    }

    @Test
    void update() {
        GelfEvent event1 = new GelfEvent();
        dataSet.update(event1, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event1.getGelfSeverity());
        assertEquals("dfs.DataNode$PacketResponder", event1.getLogger());
        assertEquals("PacketResponder 1 for block blk_38865049064139660 terminating", event1.getBodyAsString());
        assertEquals("148", event1.get("pid").asString());
        assertNotNull(event1.get("correlationalId").getValue());

        GelfEvent event2 = new GelfEvent();
        dataSet.update(event2, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event2.getGelfSeverity());
        assertEquals("dfs.DataNode$PacketResponder", event2.getLogger());
        assertEquals("PacketResponder 0 for block blk_-6952295868487656571 terminating", event2.getBodyAsString());
        assertEquals("222", event2.get("pid").asString());
        assertNotNull(event2.get("correlationalId").getValue());
    }
}