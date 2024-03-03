package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.jdbc.support.Session;
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
            LOGGER.error("Failed to persist database snapshot for database '" + snapshot.getDatabase().getName() + "'", e);
        }
    }

    private void persistStatements(Schema schema, net.microfalx.bootstrap.jdbc.support.Snapshot snapshot) {
        List<Session> sessions = snapshot.getSessions().stream().filter(session -> session.getStatement() != null).toList();
        for (Session session : sessions) {
            try {
                databaseService.persistStatement(schema, session, session.getStatement());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private Snapshot convert(Schema schema, net.microfalx.bootstrap.jdbc.support.Snapshot snapshot) throws IOException {
        Resource resource = databaseService.writeSnapshot(snapshot);
        return schema != null ? Snapshot.from(schema, snapshot, resource) : null;
    }


}
