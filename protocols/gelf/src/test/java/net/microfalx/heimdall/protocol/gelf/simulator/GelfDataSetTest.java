package net.microfalx.heimdall.protocol.gelf.simulator;

import com.cloudbees.syslog.Severity;
import net.microfalx.heimdall.protocol.core.Address;
import net.microfalx.heimdall.protocol.gelf.GelfEvent;
import net.microfalx.heimdall.protocol.gelf.simulator.loghub.ZookeeperDataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GelfDataSetTest {

    private GelfDataSet dataSet;

    @BeforeEach
    void setUp() {
        dataSet = new ZookeeperDataSet.Factory().createDataSet();
    }

    @Test
    void update() {
        GelfEvent event1 = new GelfEvent();
        dataSet.update(event1, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event1.getGelfSeverity());
        assertEquals("0:0:0:0:0:0:0:2181:FastLeaderElection", event1.getLogger());
        assertEquals("QuorumPeer[myid=1]/0", event1.getProcess());
        assertEquals("Notification time out: 3200", event1.getBodyAsString());

        GelfEvent event2 = new GelfEvent();
        dataSet.update(event2, Address.host("localhost"), Address.host("localhost"));
        assertEquals(Severity.INFORMATIONAL, event2.getGelfSeverity());
        assertEquals("3888:QuorumCnxManager$Listener", event2.getLogger());
        assertEquals("/10.10.34.11", event2.getProcess());
        assertEquals("Received connection request /10.10.34.11:45307", event2.getBodyAsString());
    }
}