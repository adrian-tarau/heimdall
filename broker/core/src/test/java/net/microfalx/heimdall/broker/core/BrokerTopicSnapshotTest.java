package net.microfalx.heimdall.broker.core;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrokerTopicSnapshotTest {

    @Test
    public void serializeSnapshot() {
        BrokerTopicSnapshot snapshot = createSnapshot();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        snapshot.serialize(outputStream);
        BrokerTopicSnapshot deserializedSnapshot = BrokerTopicSnapshot.deserialize(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(snapshot.getCount(), deserializedSnapshot.getCount());
        assertEquals(snapshot.getTotalCount(), deserializedSnapshot.getTotalCount());
        assertEquals(snapshot.getEvents().size(), deserializedSnapshot.getEvents().size());
    }

    @Test
    public void serializeEvent() {
        BrokerTopicSnapshot.Event event = createEvent(1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        event.serialize(outputStream);
        BrokerTopicSnapshot.Event deserializedEvent = BrokerTopicSnapshot.Event.deserialize(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getName(), deserializedEvent.getName());
        assertEquals(event.getAttributes().size(), deserializedEvent.getAttributes().size());
    }

    private BrokerTopicSnapshot.Event createEvent(int index) {
        return new BrokerTopicSnapshot.Event("e" + index)
                .setName("Name1").setAttributes(Map.of("k1", "v1"));
    }

    private BrokerTopicSnapshot createSnapshot() {
        BrokerTopicSnapshot snapshot = new BrokerTopicSnapshot();
        for (int i = 0; i < 10; i++) {
            snapshot.add(createEvent(i));
        }
        return snapshot;
    }

}