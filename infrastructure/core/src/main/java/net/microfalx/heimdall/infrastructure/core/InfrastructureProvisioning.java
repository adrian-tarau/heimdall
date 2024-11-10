package net.microfalx.heimdall.infrastructure.core;

import net.microfalx.heimdall.infrastructure.api.Environment;
import net.microfalx.heimdall.infrastructure.api.InfrastructureService;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.heimdall.infrastructure.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.microfalx.heimdall.infrastructure.api.InfrastructureConstants.*;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

class InfrastructureProvisioning implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureProvisioning.class);

    private final InfrastructureService infrastructureService;

    public InfrastructureProvisioning(InfrastructureService infrastructureService) {
        requireNonNull(infrastructureService);
        this.infrastructureService = infrastructureService;
    }

    @Override
    public void run() {
        try {
            provisionServices();
        } catch (Exception e) {
            LOGGER.error("Failed to provision default services", e);
        }
    }

    private void provisionServices() {
        provisionServers();
        provisionEnvironments();
        provisionCoreServices();
        provisionNetworkServices();
        provisionDatabaseServices();
        provisionOtherServices();
    }

    private void provisionCoreServices() {
        infrastructureService.registerService(Service.create(Service.Type.HTTP));
        infrastructureService.registerService(Service.create(Service.Type.SSH));
        infrastructureService.registerService(Service.create(Service.Type.ICMP));
    }

    private void provisionServers() {
        infrastructureService.registerServer(Server.get());
    }

    private void provisionEnvironments() {
        infrastructureService.registerEnvironment((Environment) Environment.create("heimdall")
                .server(Server.get())
                .tag(AUTO_TAG).tag("heimdall")
                .name("Heimdall").description("The environment represented by this instance of Heimdel")
                .build());
    }

    private void provisionNetworkServices() {
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.TCP).port(7)
                .discoverable(true)
                .tag(AUTO_TAG).tag(OS_TAG).name("Echo")
                .description("A standard TCP/IP service used primarily for testing reachability, debugging software, and identifying routing problems").build());

        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.TCP).port(25)
                .tag(AUTO_TAG).name("SMTP")
                .description("A service used to send emails (Simple Mail Transfer Protocol)").build());
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.TCP).port(465)
                .tls(true).tag(AUTO_TAG).name("SMTPs")
                .description("A service used to send emails (Simple Mail Transfer Protocol) over Secure Sockets Layer").build());

        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.TCP).port(143)
                .tag(AUTO_TAG).name("IMAP")
                .description("A service used to receive emails (Internet Message Access Protocol)").build());
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.TCP).port(143)
                .tls(true).tag(AUTO_TAG).name("IMAPs")
                .description("A service used to receive emails (Internet Message Access Protocol) over Secure Sockets Layer").build());

        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.UDP).port(53)
                .tag(AUTO_TAG).tag(OS_TAG).name("DNS")
                .description("A service used to turn the domain name into an IP address").build());
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.UDP).port(123)
                .tag(AUTO_TAG).tag(OS_TAG).name("NTP")
                .description("A service used for clock synchronization between computer systems over packet-switched, variable-latency data networks (Network Time Protocol)").build());

        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.UDP).port(161)
                .tag(AUTO_TAG).name("SNMP")
                .description("A service used to monitor and manage network devices connected over an IP (Simple Network Management Protocol)").build());
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.UDP).port(162)
                .tag(AUTO_TAG).name("SNMP Trap")
                .description("A service used to receive SNMP Traps from network devices").build());

        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.UDP).port(514)
                .tag(AUTO_TAG).tag(OS_TAG).name("Syslog")
                .description("A service used to receive logs from processes/services (system logging service)").build());
    }

    private void provisionDatabaseServices() {
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.TCP).port(3306)
                .discoverable(true).tag(AUTO_TAG).tag(DATABASE_TAG).name("MySQL")
                .description("An open-source relational database management system").build());
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.TCP).port(5432)
                .discoverable(true).tag(AUTO_TAG).tag(DATABASE_TAG).name("Postgres")
                .description("An open-source relational database management system emphasizing extensibility and SQL compliance").build());
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.TCP).port(5433)
                .discoverable(true).tag(AUTO_TAG).tag(DATABASE_TAG).name("Vertica")
                .description("An is an OLAP (online analytical processing) data warehouse management system").build());
    }

    private void provisionOtherServices() {
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.HTTP)
                .discoverable(true).port(9100).path("metrics").authType(Service.AuthType.BASIC)
                .user("${node_exporter_user}", "${node_exporter_password}")
                .tag(AUTO_TAG).name("Node Exporter").description("A service which exports health metrics for a server").build());
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.HTTP)
                .livenessPath("/-/healthy").readinessPath("/-/ready")
                .discoverable(true).port(9090).authType(Service.AuthType.BASIC)
                .user("${prometheus_user}", "${prometheus_password}")
                .tag(AUTO_TAG).name("Prometheus").description("An open source application used for event monitoring and alerting.").build());
        infrastructureService.registerService((Service) new Service.Builder().type(Service.Type.HTTP)
                .livenessPath("/api/health")
                .discoverable(true).port(3000).authType(Service.AuthType.BASIC)
                .user("${grafana_user}", "${grafana_password}")
                .tag(AUTO_TAG).name("Grafana").description("An open source analytics and interactive visualization web application").build());
    }

}
