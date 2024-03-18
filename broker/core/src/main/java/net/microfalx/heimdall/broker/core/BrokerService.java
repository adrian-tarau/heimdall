package net.microfalx.heimdall.broker.core;

import net.microfalx.bootstrap.broker.BrokerConsumer;
import net.microfalx.bootstrap.broker.BrokerUtils;
import net.microfalx.bootstrap.broker.Topic;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.core.async.TaskExecutorFactory;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.TimeUtils;
import net.microfalx.resource.FileResource;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.time.Duration.ofMinutes;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A service which manages a collection of brokers.
 */
@Service("HeimdallBrokerService")
public class BrokerService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerService.class);

    private static final DateTimeFormatter DIRECTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String FILE_NAME_FORMAT = "%09d";
    private static final LocalDateTime STARTUP = LocalDateTime.now();
    private static final Map<LocalDate, AtomicInteger> resourceSequences = new ConcurrentHashMap<>();
    private static final AtomicInteger TMP_FILE_REF = new AtomicInteger(1);

    @Autowired
    private net.microfalx.bootstrap.broker.BrokerService brokerService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private BrokerTopicRepository topicRepository;

    @Autowired
    private BrokerSessionRepository sessionRepository;

    @Autowired
    private BrokerEventRepository eventRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TaskScheduler taskScheduler;
    private AsyncTaskExecutor executor;
    private final Map<Integer, Lock> brokerLocks = new ConcurrentHashMap<>();
    private final Map<Integer, Lock> topicLocks = new ConcurrentHashMap<>();

    private Resource sessionResource;
    private Resource tmpResource;
    private final Map<Integer, Broker> brokerCache = new HashMap<>();
    private volatile List<BrokerTopic> topicCache = Collections.emptyList();
    private volatile long lastTopicCacheRefresh = TimeUtils.oneDayAgo();
    private final Map<String, BrokerConsumer<byte[], byte[]>> consumers = new ConcurrentHashMap<>();

    BrokerRepository getBrokerRepository() {
        return brokerRepository;
    }

    BrokerTopicRepository getTopicRepository() {
        return topicRepository;
    }

    BrokerSessionRepository getSessionRepository() {
        return sessionRepository;
    }

    BrokerEventRepository getEventRepository() {
        return eventRepository;
    }

    TransactionTemplate newTransaction() {
        return new TransactionTemplate(transactionManager);
    }

    Lock getLock(Broker broker) {
        requireNonNull(broker);
        return brokerLocks.computeIfAbsent(broker.getId(), s -> new ReentrantLock());
    }

    Lock getLock(BrokerTopic topic) {
        requireNonNull(topic);
        return topicLocks.computeIfAbsent(topic.getId(), s -> new ReentrantLock());
    }

    Topic getTopic(BrokerTopic topic) {
        requireNonNull(topic);
        net.microfalx.bootstrap.broker.Broker broker = getBroker(topic.getBroker());
        return Topic.create(broker, topic.getName()).withFormat(Topic.Format.RAW).withSubscription("heimdall")
                .withAutoCommit(false)
                .withClientId(BrokerUtils.createClientId("heimdall"));
    }

    BrokerConsumer<byte[], byte[]> getConsumer(Topic topic) {
        return consumers.computeIfAbsent(topic.getId(), s -> brokerService.createConsumer(topic));
    }

    void releaseConsumer(Topic topic) {
        BrokerConsumer<byte[], byte[]> consumer = consumers.remove(topic.getId());
        if (consumer != null) consumer.close();
    }

    net.microfalx.bootstrap.broker.Broker getBroker(Broker broker) {
        requireNotEmpty(broker);
        String brokerId = getBrokerId(broker);
        return brokerService.getBroker(brokerId);
    }

    List<BrokerTopic> getActiveTopics() {
        if (TimeUtils.millisSince(lastTopicCacheRefresh) > TimeUtils.ONE_MINUTE) {
            topicCache = topicRepository.findByActive(true);
        }
        return topicCache;
    }

    /**
     * Stores a collection of events collected during a session as serialized in an external resource.
     *
     * @param resource the snapshot, as a ZIP with events serialized as files
     * @return the resource where the snapshot was stored
     * @throws IOException I/O exception if snapshot cannot be stored
     */
    Resource writeSnapshot(Resource resource) throws IOException {
        Resource directory = sessionResource.resolve(DIRECTORY_DATE_FORMATTER.format(LocalDateTime.now()), Resource.Type.DIRECTORY);
        Resource target = directory.resolve(String.format(FILE_NAME_FORMAT, getNextSequence()));
        if (!directory.exists()) directory.create();
        return target.copyFrom(resource);
    }

    /**
     * Creates a temporary file to store a snapshot.
     *
     * @return a non-null instance
     */
    File getTmpFile() {
        String fileName = Long.toHexString(System.currentTimeMillis()) + "_" + TMP_FILE_REF.getAndIncrement();
        FileResource fileResource = (FileResource) tmpResource.resolve(fileName);
        return fileResource.getFile();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reload();
        initResources();
        initializeExecutor();
        scheduleTasks();
    }

    public void reload() {
        LOGGER.info("Register brokers");
        for (Broker broker : brokerRepository.findAll()) {
            brokerCache.put(broker.getId(), broker);
            String id = toIdentifier(broker.getName() + "_" + broker.getType());
            try {
                net.microfalx.bootstrap.broker.Broker.Builder builder = net.microfalx.bootstrap.broker.Broker.builder(broker.getType(), id)
                        .name(broker.getName()).timeZone(ZoneId.of(broker.getTimeZone()));
                builder.parameters(load(broker.getParameters()));
                brokerService.registerBroker(builder.build());
            } catch (Exception e) {
                LOGGER.error("Failed to register broker " + broker.getName(), e);
            }
        }
    }

    private Map<String, String> load(String props) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(props));
        Map<String, String> map = new HashMap<>();
        properties.forEach((k, v) -> map.put(ObjectUtils.toString(k), ObjectUtils.toString(v)));
        return map;
    }

    private String getBrokerId(Broker broker) {
        return toIdentifier(broker.getName() + "_" + broker.getType());
    }

    private void scheduleTasks() {
        taskScheduler.schedule(new SessionSchedulerTask(), new PeriodicTrigger(ofMinutes(1)));
    }

    private void initializeExecutor() {
        executor = TaskExecutorFactory.create("broker").setRatio(2).createExecutor();
    }

    private Resource getSharedResource() {
        return resourceService.getShared("broker");
    }

    private void initResources() {
        sessionResource = getSharedResource().resolve("session", Resource.Type.DIRECTORY);
        tmpResource = resourceService.getTransient("broker");
        try {
            tmpResource.createParents();
        } catch (IOException e) {
            LOGGER.warn("Failed to create broker session temporary storage", e);
        }
    }

    int getNextSequence() {
        return resourceSequences.computeIfAbsent(LocalDate.now(), localDate -> {
            int start = 1;
            if (STARTUP.toLocalDate().equals(localDate)) {
                start = STARTUP.toLocalTime().toSecondOfDay();
            }
            return new AtomicInteger(start);
        }).getAndIncrement();
    }

    class SessionSchedulerTask implements Runnable {

        @Override
        public void run() {
            for (BrokerTopic topic : getActiveTopics()) {
                if (topic.isActive()) executor.submit(new BrokerSessionTask(BrokerService.this, topic,
                        contentService, indexService));
            }
        }
    }
}
