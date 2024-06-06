package jpa;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Description;
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
    private Integer id;

    @Column(name = "service_id", nullable = false)
    @JoinColumn(name = "service_id")
    @Visible(value = false)
    private Integer serviceId;

    @Column(name = "server_id", nullable = false)
    @JoinColumn(name = "server_id")
    @Visible(value = false)
    private Integer serverId;

    @Column(name = "environment_id", nullable = false)
    @JoinColumn(name = "environment_id")
    @Visible(value = false)
    private Integer environmentId;

    @Column(name = "hoops")
    @Position(10)
    @Description("The number of routers/host the ping must pass though to reach the destination host")
    private Integer hoops;

    @Column(name = "connection_timeout")
    @Position(15)
    @Description("The amount of time to wait if the ping did not reach the destination host")
    private Integer connectionTimeOut;

    @Column(name = "read_timeout")
    @Position(16)
    @Description("The amount of time to wait if the ping is not read by the destination host")
    private Integer readTimeOut;

    @Column(name = "write_timeout")
    @Position(17)
    @Description("The amount of time to wait if the ping is not created by the host")
    private Integer writeTimeOut;


}
