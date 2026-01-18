package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.jdbc.support.Session;
import net.microfalx.bootstrap.support.report.Issue;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * A background task which collects database metric and stores them in a snapshot.
 */
class SnapshotsTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private final DatabaseService databaseService;

    private Collection<net.microfalx.bootstrap.jdbc.support.Snapshot> snapshots;

    SnapshotsTask(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void run() {
        if (!databaseService.getProperties().isEnabled()) return;
        takeSnapshots();
        persistSnapshots();
    }

    private void takeSnapshots() {
        snapshots = databaseService.getDatabaseService().getSnapshots();
    }

    private void persistSnapshots() {
        for (net.microfalx.bootstrap.jdbc.support.Snapshot snapshot : snapshots) {
            Schema schema = databaseService.findSchema(snapshot.getDatabase());
            if (schema == null) continue;
            persistStatements(schema, snapshot);
            persistSnapshot(schema, snapshot);
        }
    }

    private void persistSnapshot(Schema schema, net.microfalx.bootstrap.jdbc.support.Snapshot snapshot) {
        try {
            Snapshot databaseSnapshot = convert(schema, snapshot);
            if (databaseSnapshot != null) {
                databaseService.getDatabaseSnapshotRepository().saveAndFlush(databaseSnapshot);
            }
        } catch (Exception e) {
            Issue.create(Issue.Type.DATA_INTEGRITY, "Persist Snapshots").withDescription("Failed to persist database snapshots", e)
                    .withModule("Database").register();
        }
    }

    private void persistStatements(Schema schema, net.microfalx.bootstrap.jdbc.support.Snapshot snapshot) {
        List<Session> sessions = snapshot.getSessions().stream().filter(session -> session.getStatement() != null).toList();
        for (Session session : sessions) {
            try {
                databaseService.persistStatement(schema, session.getStatement());
            } catch (Exception e) {
                Issue.create(Issue.Type.DATA_INTEGRITY, "Persist Statements").withDescription("Failed to persist database statements", e)
                        .withModule("Database").register();
            }
        }
    }

    private Snapshot convert(Schema schema, net.microfalx.bootstrap.jdbc.support.Snapshot snapshot) throws IOException {
        Resource resource = databaseService.writeSnapshot(snapshot);
        return schema != null ? Snapshot.from(schema, snapshot, resource) : null;
    }


}
