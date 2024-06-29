package net.microfalx.heimdall.infrastructure.ping;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.heimdall.infrastructure.core.system.Server;
import net.microfalx.heimdall.infrastructure.core.system.Service;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "infrastructure_ping")
@Getter
@Setter
@ToString(callSuper = true)
@Name("Health Check")
public class Ping extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @ManyToOne
    @JoinColumn(name = "server_id", nullable = false)
    @Position(10)
    @Width("150")
    private Server server;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    @Position(11)
    @Width("150")
    private Service service;

    @Column(name = "active")
    @Position(15)
    @Description("Indicates whether the ping is active")
    private boolean active;

    @Column(name = "interval")
    @Position(16)
    @Description("The interval between pings")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    @Width("90")
    private int interval;

    @Column(name = "hoops")
    @Position(20)
    @Description("The number of routers/host the ping must pass though to reach the destination host")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT, Visible.Mode.ADD})
    @Width("100")
    private Integer hoops;

    @Column(name = "connection_timeout")
    @Position(30)
    @Label(group = "Timeout", value = "Connection")
    @Description("The amount of time to wait if the ping did not reach the destination host")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Integer connectionTimeOut;

    @Column(name = "read_timeout")
    @Position(31)
    @Label(group = "Timeout", value = "Read")
    @Description("The amount of time to wait if the ping is not read by the destination host")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Integer readTimeOut;

    @Column(name = "write_timeout")
    @Position(32)
    @Width("100")
    @Label(group = "Timeout", value = "Write")
    @Description("The amount of time to wait if the ping is not created by the host")
    @Formattable(unit = Formattable.Unit.MILLI_SECOND)
    private Integer writeTimeOut;


}
