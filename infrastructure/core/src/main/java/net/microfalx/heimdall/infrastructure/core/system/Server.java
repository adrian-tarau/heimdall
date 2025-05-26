package net.microfalx.heimdall.infrastructure.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.annotation.Tab;
import net.microfalx.bootstrap.dataset.annotation.Tabs;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

import java.util.Collection;

@Entity
@Table(name = "infrastructure_server")
@Name("Servers")
@Getter
@Setter
@ToString(callSuper = true)
@Tabs(attributes = {"tags", "description"})
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

    @Column(name = "time_zone", nullable = false)
    @Position(35)
    @Description("The timezone where the server is deployed")
    @Lookup(model = TimeZoneLookup.class)
    @Tab(label = "Options")
    private String timeZone;

    @Column(name = "icmp", nullable = false)
    @Position(40)
    @Description("Indicates whether the server could be pinged using ICMP protocol")
    @Label("ICMP")
    @Tab(label = "Options")
    private boolean icmp;

    @Column(name = "attributes", nullable = false)
    @Position(50)
    @Description("A collection of attributes, one per line, separated by '=' associated with a server")
    @Component(Component.Type.TEXT_AREA)
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.ADD, Visible.Mode.VIEW})
    @Tab(label = "Options")
    private String attributes;

    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @ManyToMany()
    @JoinTable(name="infrastructure_environment_to_server", joinColumns = @JoinColumn(name = "server_id"), inverseJoinColumns = @JoinColumn(name = "environment_id"))
    private Collection<Environment> environments;

    public static String toNaturalId(Server server) {
        if (server == null) return null;
        return StringUtils.toIdentifier(server.getHostname());
    }

}
