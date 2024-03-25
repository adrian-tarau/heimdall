package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.broker.BrokerConsumer;
import net.microfalx.bootstrap.broker.BrokerUtils;
import net.microfalx.bootstrap.broker.Event;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.lang.*;
import net.microfalx.metrics.Metrics;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.time.Duration.ofSeconds;
import static net.microfalx.bootstrap.search.Document.SOURCE_FIELD;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

class BrokerSessionTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerSessionTask.class);

    private static final Metrics METRICS = BrokerUtils.METRICS.withGroup("Session");

    private static final int MAX_EMPTY_ITERATIONS = 3;
    private static final int MAX_ITERATIONS = 20;
    private static final int MAX_EVENT_COUNT = 5_000;
    private static final int MAX_EVENT_SIZE = 5_000_000;
    private static final Duration MAX_DURATION = ofSeconds(10);

    private final BrokerService brokerService;
    private final ContentService contentService;
    private final IndexService indexService;
    private final BrokerTopic topic;
    private Topic realTopic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ZipOutputStream zipOutputStream;
    private File zipFile;
    private int reference = 1;

    BrokerSessionTask(BrokerService brokerService, BrokerTopic topic,
                      ContentService contentService, IndexService indexService) {
        requireNonNull(brokerService);
        requireNonNull(topic);
        this.brokerService = brokerService;
        this.contentService = contentService;
        this.indexService = indexService;
        this.topic = topic;
    }

    @Override
    public void run() {
        Lock lock = brokerService.getLock(topic);
        if (lock.tryLock()) {
            try {
                collectEvents();
            } finally {
                lock.unlock();
            }
        }
    }

    private void collectEvents() {
        this.realTopic = brokerService.getTopic(topic);
        LOGGER.info("Collect events from " + BrokerUtils.describe(realTopic));
        int iteration = 0;
        while (iteration++ < MAX_ITERATIONS) {
            try {
                BrokerTopicSnapshot snapshot = METRICS.timeCallable("Collect", this::doCollectEvents);
                METRICS.timeCallable("Persist", () -> persistSession(snapshot));
                METRICS.time("Commit", (t) -> commitConsumer());
                boolean hasMore = snapshot.getCount() > realTopic.getMaximumPollRecords();
                if (!hasMore) break;
            } catch (Exception e) {
                brokerService.releaseConsumer(this.realTopic);
                LOGGER.error("Failed to collect events from " + BrokerUtils.describe(this.realTopic), e);
                persistSession(BrokerSession.Status.FAILED, new BrokerTopicSnapshot(), null, ExceptionUtils.getRootCauseMessage(e));
            }
        }
    }

    private BrokerTopicSnapshot doCollectEvents() throws IOException {
        this.startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plus(MAX_DURATION);
        BrokerTopicSnapshot snapshot = new BrokerTopicSnapshot();
        boolean shouldSample = topic.getSampleSize() != null;
        int totalSize = 0;
        BrokerConsumer<byte[], byte[]> consumer = brokerService.getConsumer(brokerService.getTopic(topic));
        int emptyCounter = 0;
        int sampleCount = getSampleCounter(topic);
        while (emptyCounter <= MAX_EMPTY_ITERATIONS && LocalDateTime.now().isBefore(endTime)) {
            Collection<Event<byte[], byte[]>> events = consumer.poll();
            if (events.isEmpty()) {
                emptyCounter++;
            } else {
                emptyCounter = 0;
            }
            for (Event<byte[], byte[]> event : events) {
                BrokerTopicSnapshot.Event brokerEvent = new BrokerTopicSnapshot.Event(event.getId())
                        .setKey(event.getKey()).setValue(event.getValue())
                        .setTimestamp(TimeUtils.toMillis(event.getTimestamp()))
                        .setPartition(ObjectUtils.toString(event.getOffset().getPartition().getValue()));
                boolean acceptEvent = !shouldSample || sampleCount == 0;
                if (acceptEvent) {
                    totalSize += brokerEvent.getSize();
                    snapshot.add(brokerEvent);
                    writeEvent(brokerEvent);
                }
                snapshot.add(brokerEvent.getSize());
                if (sampleCount == 0) sampleCount = getSampleCounter(topic);
                sampleCount--;
            }
            if (snapshot.getCount() >= MAX_EVENT_COUNT || totalSize > MAX_EVENT_SIZE) break;
        }
        BrokerUtils.METRICS.count(topic.getName(), snapshot.getTotalCount());
        return snapshot;
    }

    private void commitConsumer() {
        BrokerConsumer<byte[], byte[]> consumer = brokerService.getConsumer(brokerService.getTopic(topic));
        consumer.commit();
    }

    private int getSampleCounter(BrokerTopic topic) {
        return topic.getSampleSize() != null ? topic.getSampleSize() : Integer.MAX_VALUE;
    }

    private void writeEvent(BrokerTopicSnapshot.Event event) throws IOException {
        ZipOutputStream zipOutputStream = getZipOutputStream();
        ZipEntry entry = new ZipEntry(StringUtils.toIdentifier(event.getId()));
        zipOutputStream.putNextEntry(entry);
        event.serialize(IOUtils.getUnclosableOutputStream(zipOutputStream));
        zipOutputStream.closeEntry();
    }

    private BrokerSession persistSession(BrokerTopicSnapshot snapshot) throws IOException {
        zipOutputStream.close();
        zipOutputStream = null;
        Resource resource = brokerService.writeSnapshot(FileResource.file(zipFile));
        TransactionTemplate transactionTemplate = brokerService.newTransaction();
        return transactionTemplate.execute(status -> {
            BrokerSession session = persistSession(BrokerSession.Status.SUCCESSFUL, snapshot, resource, null);
            for (BrokerTopicSnapshot.Event event : snapshot.getEvents()) {
                indexEvent(session, event);
                persistEvent(session, event);
            }
            return session;
        });
    }

    private ZipOutputStream getZipOutputStream() throws IOException {
        if (zipOutputStream == null) {
            zipFile = brokerService.getTmpFile();
            zipOutputStream = new ZipOutputStream(IOUtils.getBufferedOutputStream(zipFile));
        }
        return zipOutputStream;
    }

    private void indexEvent(BrokerSession session, BrokerTopicSnapshot.Event event) {
        Resource resource = MemoryResource.create(event.getValue());
        String mediaType = resource.getMimeType();
        if (MimeType.APPLICATION_OCTET_STREAM.equals(mediaType)) return;
        URI uri = net.microfalx.heimdall.broker.core.BrokerUtils.getEventUri(session.getResource(), event.getId());
        Document document = Document.create(toIdentifier(realTopic.getId() + "_" + event.getId()),
                realTopic.getBroker().getName() + " / " + realTopic.getName() + " / " + event.getId());
        document.setOwner("broker");
        document.setType("event");
        document.setLength(event.getSize());
        document.add(Attribute.create("broker", realTopic.getBroker().getName()).enableAll());
        document.add(Attribute.create("partition", event.getPartition()));
        document.add(Attribute.create(SOURCE_FIELD, realTopic.getName()).enableAll());
        document.setBody(resource);
        document.setBodyUri(uri);
        document.setMimeType(mediaType);
        document.setCreatedAt(TimeUtils.toLocalDateTime(event.getTimestamp()));
        document.setReceivedAt(LocalDateTime.now());
        indexService.index(document, false);
    }

    private void persistEvent(BrokerSession session, BrokerTopicSnapshot.Event event) {
        BrokerEvent brokerEvent = new BrokerEvent();
        brokerEvent.setBroker(topic.getBroker());
        brokerEvent.setTopic(topic);
        brokerEvent.setType(topic.getBroker().getType());
        brokerEvent.setSession(session);
        brokerEvent.setCreatedAt(TimeUtils.toLocalDateTime(event.getTimestamp()));
        brokerEvent.setReceivedAt(startTime);
        brokerEvent.setEventId(event.getId());
        brokerEvent.setEventName(event.getName());
        brokerService.getEventRepository().save(brokerEvent);
    }

    private BrokerSession persistSession(BrokerSession.Status status, BrokerTopicSnapshot snapshot, Resource resource, String failureMessage) {
        endTime = LocalDateTime.now();
        BrokerSession session = new BrokerSession();
        session.setBroker(topic.getBroker());
        session.setTopic(topic);
        session.setType(realTopic.getBroker().getType());
        session.setTotalEventCount(snapshot.getTotalCount());
        session.setTotalEventSize(snapshot.getTotalSize());
        session.setSampledEventCount(snapshot.getCount());
        session.setSampledEventSize(snapshot.getSize());
        session.setStartedAt(startTime);
        session.setEndedAt(endTime);
        session.setDuration(Duration.between(startTime, endTime));
        session.setStatus(status);
        session.setFailureMessage(failureMessage);
        session.setResource(resource != null ? resource.toURI().toASCIIString() : null);
        brokerService.getSessionRepository().saveAndFlush(session);
        return session;
    }
}
