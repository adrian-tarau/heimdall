package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.broker.BrokerConsumer;
import net.microfalx.bootstrap.broker.BrokerUtils;
import net.microfalx.bootstrap.broker.Event;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.bootstrap.content.Content;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.search.Attribute;
import net.microfalx.bootstrap.search.Document;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.bootstrap.search.SearchUtils;
import net.microfalx.bootstrap.template.Template;
import net.microfalx.bootstrap.template.TemplateContext;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import static java.time.Duration.ofSeconds;
import static net.microfalx.bootstrap.search.Document.SOURCE_FIELD;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.*;

class BrokerSessionTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerSessionTask.class);

    private static final Metrics METRICS = BrokerUtils.METRICS.withGroup("Session");

    private static final int MAX_EMPTY_ITERATIONS = 3;
    private static final int MAX_ITERATIONS = 20;
    private static final int MAX_EVENT_COUNT = 1_000;
    private static final int MAX_EVENT_SIZE = 5_000_000;
    private static final int MAX_ATTRIBUTE_LENGTH = 50;
    private static final Duration MAX_DURATION = ofSeconds(10);

    private final BrokerService brokerService;
    private final ContentService contentService;
    private final IndexService indexService;
    private final BrokerTopic topic;

    private Template nameTemplate;
    private Template descriptionTemplate;
    private TemplateContext templateContext;

    private Collection<Pattern> attributeInclusions = new ArrayList<>();
    private Collection<Pattern> attributeExclusions = new ArrayList<>();
    private Collection<String> attributePrefixes = new ArrayList<>();

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

    BrokerSessionTask setNameTemplate(Template nameTemplate) {
        this.nameTemplate = nameTemplate;
        return this;
    }

    BrokerSessionTask setDescriptionTemplate(Template descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
        return this;
    }

    BrokerSessionTask setTemplateContext(TemplateContext templateContext) {
        this.templateContext = templateContext;
        return this;
    }

    void addAttributeInclusion(String pattern) {
        if (StringUtils.isEmpty(pattern)) return;
        attributeInclusions.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
    }

    void addAttributeExclusion(String pattern) {
        if (StringUtils.isEmpty(pattern)) return;
        attributeExclusions.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
    }

    void addAttributePrefix(String prefix) {
        if (StringUtils.isEmpty(prefix)) return;
        attributePrefixes.add(prefix.toLowerCase());
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
        LOGGER.debug("Collect events from " + BrokerUtils.describe(realTopic));
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
                    extractNameAndDescription(brokerEvent);
                    totalSize += brokerEvent.getSize();
                    writeEvent(brokerEvent);
                    snapshot.add(brokerEvent);
                }
                snapshot.add(brokerEvent.getSize());
                if (sampleCount == 0) sampleCount = getSampleCounter(topic);
                sampleCount--;
            }
            if (snapshot.getCount() >= MAX_EVENT_COUNT || totalSize >= MAX_EVENT_SIZE) break;
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
        try {
            zipOutputStream.putNextEntry(entry);
        } catch (ZipException e) {
            String message = defaultIfNull(e.getMessage(), EMPTY_STRING);
            if (message.startsWith("duplicate entry:")) return;
            ExceptionUtils.throwException(e);
        }
        event.serialize(IOUtils.getUnclosableOutputStream(zipOutputStream));
        zipOutputStream.closeEntry();
    }

    private void extractNameAndDescription(BrokerTopicSnapshot.Event event) throws IOException {
        Map<String, Object> attributes = new HashMap<>();
        event.setAttributes(attributes);
        Content content = contentService.extract(MemoryResource.create(event.getValue()), true);
        if (nameTemplate != null) {
            event.setName(nameTemplate.evaluate(templateContext.withAttributes(content.getAttributes())));
        }
        if (isEmpty(event.getName())) {
            event.setName(realTopic.getBroker().getName() + " / " + realTopic.getName() + " / " + event.getId());
        }
        if (descriptionTemplate != null) {
            event.setDescription(descriptionTemplate.evaluate(templateContext.withAttributes(content.getAttributes())));
        }
        content.getAttributes().forEach(attribute -> {
            if (acceptAttribute(attribute)) {
                String name = renameAttribute(attribute.getName());
                attributes.put(name, attribute.getValue());
            }
        });
    }

    private BrokerSession persistSession(BrokerTopicSnapshot snapshot) throws IOException {
        IOUtils.closeQuietly(zipOutputStream);
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
                event.getName());
        document.setDescription(event.getDescription());
        document.setOwner("broker");
        document.setType("event");
        document.setLength(event.getSize());
        document.setBody(resource);
        document.setBodyUri(uri);
        document.setMimeType(mediaType);
        document.setCreatedAt(TimeUtils.toLocalDateTime(event.getTimestamp()));
        document.setReceivedAt(LocalDateTime.now());
        document.setModifiedAt(LocalDateTime.now());
        event.getAttributes().forEach((k, v) -> {
            document.add(Attribute.create(k, v).enableAll());
        });
        document.add(Attribute.create("broker", realTopic.getBroker().getName()).enableAll());
        document.add(Attribute.create("partition", event.getPartition()));
        document.add(Attribute.create(SOURCE_FIELD, realTopic.getName()).enableAll());
        indexService.index(document, false);
    }

    private String renameAttribute(String name) {
        for (String attributePrefix : attributePrefixes) {
            if (name.toLowerCase().startsWith(attributePrefix) && name.length() > attributePrefix.length()) {
                return uncapitalizeFirst(name.substring(attributePrefix.length()));
            }
        }
        return name;
    }

    private boolean acceptAttribute(net.microfalx.bootstrap.model.Attribute attribute) {
        String value = attribute.asString();
        if (StringUtils.isEmpty(value) || SearchUtils.isStandardFieldName(attribute.getName())
                || "null".equalsIgnoreCase(value)) {
            return false;
        }
        for (Pattern pattern : attributeInclusions) {
            if (pattern.matcher(attribute.getName()).matches()) return true;
        }
        if (containsNewLines(value) || value.length() > MAX_ATTRIBUTE_LENGTH) return false;
        for (Pattern pattern : attributeExclusions) {
            if (pattern.matcher(attribute.getName()).matches()) return false;
        }
        return attributeInclusions.isEmpty();
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
        brokerEvent.setEventName(org.apache.commons.lang3.StringUtils.abbreviate(event.getName(), 190));
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
