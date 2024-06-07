package net.microfalx.heimdall.infrastructure.core;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "infrastructure_service")
@Name("Services")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class Service extends NamedTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    @Visible(value = false)
    private Integer id;

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Position(20)
    @Description("The type of the service")
    private net.microfalx.heimdall.infrastructure.api.Service.Type type = net.microfalx.heimdall.infrastructure.api.Service.Type.HTTPS;

    @Column(name = "port", nullable = false)
    @Position(30)
    @Description("The port where the service can be accessed")
    @Formattable(negativeValue = Formattable.NA)
    private int port;

    @Column(name = "path", nullable = false)
    @Position(40)
    @Description("The path to access the service (HTTP/HTTPs protocol)")
    private String path;

    @Column(name = "auth_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Position(50)
    @Description("The authentication type")
    private net.microfalx.heimdall.infrastructure.api.Service.AuthType authType = net.microfalx.heimdall.infrastructure.api.Service.AuthType.NONE;

    @Column(name = "username", nullable = false)
    @Position(60)
    @Description("The user name used to access the service (Basic authentication only)")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private String username;

    @Column(name = "password", nullable = false)
    @Position(61)
    @Description("The password used to access the service (Basic authentication only)")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private String password;

    @Column(name = "token", nullable = false)
    @Position(62)
    @Description("The token used to access the service (Bearer & Rest API authentication only)")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private String token;

    @Column(name = "connection_timeout")
    @Position(70)
    @Label("Connection Timeout")
    @Description("The amount of time to wait to establish a connect to the service")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private Integer connectionTimeOut;

    @Column(name = "read_timeout")
    @Position(71)
    @Label("Read Timeout")
    @Description("The amount of time to wait to read a response from the service")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private Integer readTimeOut;

    @Column(name = "write_timeout")
    @Position(72)
    @Label("Write Timeout")
    @Description("The amount of time to wait to write a request to the service")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private Integer writeTimeOut;

    public static String toNaturalId(Service service) {
        if (service == null) return null;
        return new net.microfalx.heimdall.infrastructure.api.Service.Builder()
                .type(service.getType())
                .port(service.getPort())
                .path(service.getPath())
                .build().getId();
    }

}
