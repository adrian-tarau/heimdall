package net.microfalx.heimdall.database.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import net.microfalx.bootstrap.jdbc.support.Database;
import net.microfalx.bootstrap.jdbc.support.DatabaseUtils;
import net.microfalx.bootstrap.jdbc.support.Statement;
import net.microfalx.bootstrap.metrics.util.SimpleStatisticalSummary;
import net.microfalx.lang.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A background task which collects statements metric and stores them.
 */
public class StatementTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private static final int NOW_THRESHOLD_SECONDS = 10;
    private static final int INTERVAL_MINUTES = 5;

    private final DatabaseService databaseService;
    private final StatementStatisticsRepository statementStatisticsRepository;
    private final LoadingCache<Integer, StatementStatistics> cachedStatistics = CacheBuilder.newBuilder().maximumSize(5000)
            .build(com.google.common.cache.CacheLoader.from(this::findStatistics));

    private Map<Database, List<Statement>> statementsByDatabase;
    private LocalDateTime threshold;

    StatementTask(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.statementStatisticsRepository = databaseService.getStatementStatisticsRepository();
    }

    @Override
    public void run() {
        threshold = databaseService.getStatementCollectionThreshold();
        if (threshold.isAfter(LocalDateTime.now().minusSeconds(NOW_THRESHOLD_SECONDS))) return;
        collectStatements();
        persistStatements();
        adjustInterval();
    }

    private void collectStatements() {
        statementsByDatabase = databaseService.getDatabaseService().getStatements(threshold.minusMinutes(INTERVAL_MINUTES), threshold)
                .stream().collect(Collectors.groupingBy(statement -> statement.getNode().getDatabase()));
    }

    private void persistStatements() {
        for (Map.Entry<Database, List<Statement>> entry : statementsByDatabase.entrySet()) {
            Schema schema = databaseService.findSchema(entry.getKey());
            for (List<Statement> statements : statementsByDatabase.values()) {
                for (Statement statement : statements) {
                    int key = databaseService.persistStatement(schema, statement);
                    try {
                        DatabaseUtils.METRICS.time("Statement Stats - Updated", (t) -> updateStats(statement, key));
                    } catch (Exception e) {
                        DatabaseUtils.METRICS.count("Statement Stats - Failed");
                        LOGGER.debug("Failed to update statistics for statement {}, root cause: {}", key, ExceptionUtils.getStackTrace(e));
                    }
                }
            }

        }
    }

    private void adjustInterval() {
        threshold = threshold.plusMinutes(INTERVAL_MINUTES);
        databaseService.setStatementCollectionThreshold(threshold);
    }

    private void updateStats(Statement statement, int key) {
        Statement.Statistics statistics = statement.getStatistics();
        if (statistics.getN() == 0) return;
        net.microfalx.heimdall.database.core.StatementStatistics statementStatistics = cachedStatistics.getUnchecked(key);
        SimpleStatisticalSummary statisticalSummary = new SimpleStatisticalSummary();
        if (statementStatistics.getExecutionCount() > 0) {
            statisticalSummary.setN(statementStatistics.getExecutionCount())
                    .setSum(statementStatistics.getTotalDuration())
                    .setMin(statementStatistics.getMinDuration())
                    .setMax(statementStatistics.getMaxDuration());
        }
        statisticalSummary.add(statement.getStatistics());
        statementStatistics.setExecutionCount(statisticalSummary.getN())
                .setTotalDuration((float) statisticalSummary.getSum())
                .setAvgDuration((float) statisticalSummary.getMean())
                .setMinDuration((float) statisticalSummary.getMin())
                .setMaxDuration((float) statisticalSummary.getMax());
        statementStatistics.setModifiedAt(statement.getExecutionTime().toLocalDateTime());
        statementStatisticsRepository.saveAndFlush(statementStatistics);
    }

    private StatementStatistics findStatistics(int key) {
        return statementStatisticsRepository.findById(key).orElse(null);
    }

}
