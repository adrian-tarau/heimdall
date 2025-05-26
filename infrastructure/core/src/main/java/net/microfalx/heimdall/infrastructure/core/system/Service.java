package net.microfalx.heimdall.infrastructure.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.annotation.Tab;
import net.microfalx.bootstrap.dataset.annotation.Tabs;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "infrastructure_service")
@Name("Services")
@Getter
@Setter
@ToString(callSuper = true)
@Tabs(attributes = {"tags", "description"})
public class Service extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    private static final int DEFAULT_TIMEOUT = 5000;

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Position(20)
    @Description("The type of the service")
    private net.microfalx.heimdall.infrastructure.api.Service.Type type = net.microfalx.heimdall.infrastructure.api.Service.Type.HTTP;

    @Column(name = "port", nullable = false)
    @Position(30)
    @Description("The port where the service can be accessed")
    @Formattable(negativeValue = Formattable.NA, prettyPrint = false)
    private int port;

    @Column(name = "path", nullable = false)
    @Label(value = "Base", group = "Paths")
    @Position(40)
    @Description("The path to access the service (HTTP/HTTPs protocol)")
    private String path;

    @Column(name = "liveness_path", nullable = false)
    @Label(value = "Liveness", group = "Paths")
    @Position(41)
    @Description("The path used to determine if the service is alive or dead (HTTP/HTTPs protocol)")
    private String livenessPath;

    @Column(name = "readiness_path", nullable = false)
    @Label(value = "Readiness", group = "Paths")
    @Position(42)
    @Description("The path used to determine if the service is ready to serve traffic (HTTP/HTTPs protocol)")
    private String readinessPath;

    @Column(name = "metrics_path", nullable = false)
    @Label(value = "Metrics", group = "Paths")
    @Position(43)
    @Description("The path used to extract metrics from a service (HTTP/HTTPs protocol)")
    private String metricsPath;

    @Column(name = "auth_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Position(50)
    @Description("The authentication type")
    private net.microfalx.heimdall.infrastructure.api.Service.AuthType authType = net.microfalx.heimdall.infrastructure.api.Service.AuthType.NONE;

    @Column(name = "username", nullable = false)
    @Position(60)
    @Description("The user name used to access the service (Basic authentication only)")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    @Tab(label = "Security")
    private String username;

    @Column(name = "password", nullable = false)
    @Position(61)
    @Description("The password used to access the service (Basic authentication only)")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    @Tab(label = "Security")
    private String password;

    @Column(name = "token", nullable = false)
    @Position(62)
    @Description("The token used to access the service (Bearer & Rest API authentication only)")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    @Tab(label = "Security")
    private String token;

    @Column(name = "discoverable", nullable = false)
    @Position(70)
    @Description("Indicates that the service will be discovered automatically and it's status monitored")
    @Tab(label = "Options")
    private boolean discoverable;

    @Column(name = "tls", nullable = false)
    @Position(71)
    @Label("TLS")
    @Description("Indicates that the service will use Transport Layer Security (TLS) to protect the communication between clients and service. TLS is the upgraded version of SSL that fixes existing SSL vulnerabilities")
    @Tab(label = "Options")
    private boolean tls;

    @Column(name = "connection_timeout")
    @Position(80)
    @Label(group = "Timeout", value = "Connection")
    @Description("The amount of time to wait to establish a connect to the service")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Tab(label = "Options")
    private int connectionTimeOut = DEFAULT_TIMEOUT;

    @Column(name = "read_timeout")
    @Position(81)
    @Label(group = "Timeout", value = "Read")
    @Description("The amount of time to wait to read a response from the service")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Tab(label = "Options")
    private int readTimeOut = DEFAULT_TIMEOUT;

    @Column(name = "write_timeout")
    @Position(82)
    @Label(group = "Timeout", value = "Write")
    @Description("The amount of time to wait to write a request to the service")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Tab(label = "Options")
    private int writeTimeOut = DEFAULT_TIMEOUT;

    public static String toNaturalId(Service service) {
        if (service == null) return null;
        return new net.microfalx.heimdall.infrastructure.api.Service.Builder()
                .type(service.getType())
                .port(service.getPort())
                .path(service.getPath())
                .build().getId();
    }

}
