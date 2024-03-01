package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.jdbc.support.Node;
import net.microfalx.bootstrap.jdbc.support.Session;

import java.util.Collection;

/**
 * A background task which collects database metric and stores them in a snapshot.
 */
class DatabaseSnapshotsTask implements Runnable {

    private final DatabaseService databaseService;

    private Collection<Node> nodes;
    private Collection<Session> sessions;

    DatabaseSnapshotsTask(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void run() {
        takeSnapshot();
        persistSnapshot();
    }

    private void takeSnapshot() {
        nodes = databaseService.getDatabaseService().getNodes();
        sessions = databaseService.getDatabaseService().getSessions();
    }

    private void persistSnapshot() {

    }
}
