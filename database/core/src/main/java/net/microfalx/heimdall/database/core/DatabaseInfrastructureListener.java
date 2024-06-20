package net.microfalx.heimdall.database.core;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.jdbc.support.Database;
import net.microfalx.bootstrap.jdbc.support.DatabaseService;
import net.microfalx.bootstrap.jdbc.support.Node;
import net.microfalx.heimdall.infrastructure.api.Cluster;
import net.microfalx.heimdall.infrastructure.api.InfrastructureListener;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.annotation.Provider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Provider
public class DatabaseInfrastructureListener extends ApplicationContextSupport implements InfrastructureListener {

    @Override
    public void onInfrastructureInitialization() {
        InfrastructureService infrastructureService = getBean(InfrastructureService.class);
        List<Database> databases = new ArrayList<>(getBean(DatabaseService.class).getDatabases());
        databases.sort(Comparator.comparing(Identifiable::getId));
        for (Database database : databases) {
            Cluster.Builder clusterBuilder = null;
            if (database.getNodes().size() > 2) {
                clusterBuilder = (Cluster.Builder) new Cluster.Builder("db_" + database.getId())
                        .zoneId(database.getZoneId()).tag("db").name(database.getName())
                        .description(database.getDescription());
            }
            for (Node node : database.getNodes()) {
                Server server = (Server) new Server.Builder().hostname(node.getHostname())
                        .tag("db").name(node.getDisplayName()).build();
                if (clusterBuilder != null) {
                    clusterBuilder.server(server);
                } else {
                    infrastructureService.registerServer(server);
                }
            }
            if (clusterBuilder != null) infrastructureService.registerCluster(clusterBuilder.build());
        }
    }
}
