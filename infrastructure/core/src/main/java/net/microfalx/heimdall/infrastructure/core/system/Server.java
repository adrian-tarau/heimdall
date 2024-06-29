package net.microfalx.heimdall.infrastructure.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "infrastructure_server")
@Name("Servers")
@Getter
@Setter
@ToString(callSuper = true)
public class Server extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @ManyToOne
    @JoinColumn(name = "cluster_id")
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
    @Label("ICMP")
    private boolean icmp;

    public static String toNaturalId(Server server) {
        if (server == null) return  null;
        return StringUtils.toIdentifier(server.getHostname());
    }

}
