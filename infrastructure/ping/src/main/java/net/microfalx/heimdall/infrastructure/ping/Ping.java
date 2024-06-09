package net.microfalx.heimdall.infrastructure.ping;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.heimdall.infrastructure.core.Server;
import net.microfalx.heimdall.infrastructure.core.Service;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Label;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

@Entity
@Table(name = "infrastructure_ping")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class Ping extends NamedTimestampAware {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Visible(value = false)
    private Integer id;

    @JoinColumn(name = "service_id", nullable = false)
    @ManyToOne
    @Position(10)
    private Service service;

    @JoinColumn(name = "server_id", nullable = false)
    @ManyToOne
    @Position(11)
    private Server server;

    @Column(name = "active")
    @Position(15)
    @Description("Indicates whether the ping is active")
    private boolean active;

    @Column(name = "interval")
    @Position(16)
    @Description("The interval between pings")
    private int interval;

    @Column(name = "hoops")
    @Position(20)
    @Description("The number of routers/host the ping must pass though to reach the destination host")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private Integer hoops;

    @Column(name = "connection_timeout")
    @Formattable()
    @Position(30)
    @Label("Connection Timeout")
    @Description("The amount of time to wait if the ping did not reach the destination host")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private Integer connectionTimeOut;

    @Column(name = "read_timeout")
    @Formattable()
    @Position(31)
    @Label("Read Timeout")
    @Description("The amount of time to wait if the ping is not read by the destination host")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private Integer readTimeOut;

    @Column(name = "write_timeout")
    @Formattable()
    @Position(32)
    @Label("Write Timeout")
    @Description("The amount of time to wait if the ping is not created by the host")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    private Integer writeTimeOut;


}
