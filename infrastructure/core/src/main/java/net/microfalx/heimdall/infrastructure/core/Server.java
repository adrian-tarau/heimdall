package net.microfalx.heimdall.infrastructure.core;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "infrastructure_server")
@Name("Servers")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class Server extends NamedTimestampAware {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    @Position(20)
    @Description("The cluster which owns the server")
    private Cluster cluster;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Position(10)
    @Description("The type of server")
    private net.microfalx.heimdall.infrastructure.api.Server.Type type = net.microfalx.heimdall.infrastructure.api.Server.Type.VIRTUAL;

    @Column(name = "hostname", nullable = false)
    @Position(30)
    @Description("The hostname of the server")
    private String hostname;

    @Column(name = "icmp", nullable = false)
    @Position(40)
    @Description("Indicates whether the server could be pinged using ICMP protocol")
    private boolean icmp;

    public static String toNaturalId(Server server) {
        if (server == null) return  null;
        return StringUtils.toIdentifier(server.getHostname());
    }

}
