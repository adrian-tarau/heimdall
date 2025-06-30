package net.microfalx.heimdall.protocol.smtp.simulator;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.heimdall.protocol.core.simulator.ProtocolDataSet;
import net.microfalx.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApacheMBoxDataSet extends MBoxDataSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheMBoxDataSet.class);

    private static final String BASE_URI = "https://mail-archives.apache.org/mod_mbox/";
    private static final LocalDate START_DATE = LocalDate.now().minusMonths(1);
    private static final LocalDate END_DATE = LocalDate.now().minusYears(5);

    private final Map<String, MBoxDataSet> dataSets = new ConcurrentHashMap<>();
    private final Map<String, LocalDate> dataSetsDates = new ConcurrentHashMap<>();
    private final Map<String, Iterator<MimeMessage>> iterators = new ConcurrentHashMap<>();
    private final Iterator<MimeMessage> iterator = new IteratorImpl();
    private Iterator<Iterator<MimeMessage>> nextDataSetIterator;

    public ApacheMBoxDataSet(Resource resource) {
        super(resource);
    }

    private static String getFileReference(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        return formatter.format(date) + ".mbox";
    }

    @Override
    public @NotNull Iterator<MimeMessage> iterator() {
        initDataSets();
        return iterator;
    }

    private MimeMessage getNext() {
        for (int i = 0; i < MAIL_BOXES.length; i++) {
            if (nextDataSetIterator == null || !nextDataSetIterator.hasNext()) {
                nextDataSetIterator = iterators.values().iterator();
            }
            if (nextDataSetIterator.hasNext()) {
                Iterator<MimeMessage> secondIterator = nextDataSetIterator.next();
                if (secondIterator.hasNext()) {
                    return secondIterator.next();
                }
            }
        }
        return null;
    }

    private LocalDate getNextDate(String mailBox) {
        LocalDate date;
        if (dataSetsDates.containsKey(mailBox)) {
            date = dataSetsDates.get(mailBox).minusMonths(1);
        } else {
            date = START_DATE;
        }
        dataSetsDates.put(mailBox, date);
        return date.withDayOfMonth(1);
    }

    private void initDataSets() {
        if (!dataSets.isEmpty()) return;
        LOGGER.info("Initializing Apache MBox data sets");
        for (String mailBox : MAIL_BOXES) {
            createDataSet(mailBox);
        }
        LOGGER.info("Apache MBox initialized with {} data sets", dataSets.size());
    }

    private void createDataSet(String mailBox) {
        try {
            for (; ; ) {
                LocalDate date = getNextDate(mailBox);
                Resource resource = getResource().resolve(mailBox, Resource.Type.DIRECTORY).resolve(getFileReference(date));
                LOGGER.info("Creating data set for mailbox {} for date {}", mailBox, date);
                MBoxDataSet dataSet = new MBoxDataSet(resource).updateName(mailBox);
                if (dataSet.getResource().length() < 1000L) {
                    LOGGER.info("No data found for mailbox {} for date {}", mailBox, date);
                } else {
                    dataSets.put(mailBox, dataSet);
                    iterators.put(mailBox, dataSet.iterator());
                    break;
                }
                if (date.isBefore(END_DATE)) break;
            }
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Failed to create data set for mailbox {}: {}", mailBox, e.getMessage());
        }
    }

    private class IteratorImpl implements Iterator<MimeMessage> {

        private MimeMessage next;

        @Override
        public boolean hasNext() {
            next = getNext();
            return next != null;
        }

        @Override
        public MimeMessage next() {
            return next;
        }
    }

    public static class Factory extends MBoxDataSet.Factory {

        public Factory() {
            super(Resource.url(BASE_URI));
            setName("Apache MBox Data Set");
        }

        @Override
        public ProtocolDataSet<MimeMessage, Field<MimeMessage>, String> createDataSet() {
            return new ApacheMBoxDataSet(getResource());
        }
    }

    public static final String[] MAIL_BOXES = {"kafka-dev", "maven-dev"};

    public static final String[] MAIL_BOXES2 = {
            "kafka-users", "maven-users",
            "kafka-dev", "maven-dev", "poi-dev",
            "pulsar-users", "solr-users",
            "pulsar-dev", "solr-dev", "lucene-dev",
            "spark-user", "spark-dev"
    };
}
