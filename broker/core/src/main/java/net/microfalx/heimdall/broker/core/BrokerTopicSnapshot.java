package net.microfalx.heimdall.broker.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A snapshot of a topic.
 */
@ToString
public class BrokerTopicSnapshot {

    private Collection<Event> events = new ArrayList<>();
    private int totalCount;
    private long totalSize;

    /**
     * Returns the total number of consumed.
     *
     * @return a positive integer
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Returns the total size of the consumed
     *
     * @return a positive integer
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Returns all the events.
     *
     * @return a non-null instance
     */
    public Collection<Event> getEvents() {
        return Collections.unmodifiableCollection(events);
    }

    /**
     * Returns the number of events in the snapshot.
     *
     * @return a positive integer
     */
    public int getCount() {
        return events.size();
    }

    /**
     * Returns the size of events in the snapshot.
     *
     * @return a positive integer
     */
    public long getSize() {
        return events.stream().mapToLong(Event::getSize).sum();
    }

    /**
     * Adds an event to the snapshot.
     *
     * @param event
     */
    void add(Event event) {
        requireNonNull(event);
        events.add(event);
    }

    /**
     * Adds the size of a consumed event.
     *
     * @param size the size in bytes
     */
    void add(long size) {
        totalCount++;
        totalSize += size;
    }

    /**
     * Serializes a snapshot.
     *
     * @param outputStream the output stream
     */
    public void serialize(OutputStream outputStream) {
        requireNotEmpty(outputStream);
        Output output = new Output(outputStream);
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.writeClassAndObject(output, this);
        output.close();
    }

    /**
     * Deserializes a snapshot.
     *
     * @param inputStream the input stream
     * @return a non-null instance
     */
    public static BrokerTopicSnapshot deserialize(InputStream inputStream) {
        requireNotEmpty(inputStream);
        Input input = new Input(inputStream);
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        return (BrokerTopicSnapshot) kryo.readClassAndObject(input);
    }

    /**
     * Holds an event collected from a topic.
     */
    @Setter
    @Getter
    @Data
    public static class Event implements Identifiable<String> {

        private static final int OVERHEAD = 40;

        private String id;
        private String partition;
        private byte[] key;
        private byte[] value;
        private long timestamp;

        public Event() {
            this(StringUtils.NA_STRING);
        }

        public Event(String id) {
            requireNotEmpty(id);
            this.id = id;
        }

        /**
         * Returns the size in bytes of the event
         *
         * @return a positive integer
         */
        public int getSize() {
            return (key != null ? key.length : 0) + (value != null ? value.length : 0) + OVERHEAD;
        }

        /**
         * Serializes a snapshot.
         *
         * @param outputStream the output stream
         */
        public void serialize(OutputStream outputStream) {
            requireNotEmpty(outputStream);
            Output output = new Output(outputStream);
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.writeClassAndObject(output, this);
            output.close();
        }

        /**
         * Deserializes a snapshot.
         *
         * @param inputStream the input stream
         * @return a non-null instance
         */
        public static Event deserialize(InputStream inputStream) {
            requireNotEmpty(inputStream);
            Input input = new Input(inputStream);
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            return (Event) kryo.readClassAndObject(input);
        }
    }
}
