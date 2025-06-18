package net.microfalx.heimdall.infrastructure.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.*;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

import java.util.Collection;

@Entity
@Table(name = "infrastructure_environment")
@Name("Environments")
@Getter
@Setter
@ToString(callSuper = true)
@Tabs(attributes = {"tags", "description"})
public class Environment extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "base_uri", nullable = false)
    @Position(30)
    @Description("The base URL for the web application server")
    @Label(group = "URI", value = "Base")
    @Filterable
    private String baseUri;

    @Column(name = "app_path", nullable = false)
    @Position(31)
    @Description("The path of the web application (user interface, appended to the base URI)")
    @Label(group = "URI", value = "Application")
    @Filterable
    private String appPath;

    @Column(name = "api_path", nullable = false)
    @Position(32)
    @Description("The path of the Rest API (appended to the base URI)")
    @Label(group = "URI", value = "Rest API")
    @Filterable
    private String apiPath;

    @Column(name = "attributes", nullable = false)
    @Position(50)
    @Description("A collection of attributes, one per line, separated by '=' associated with an environment")
    @Component(Component.Type.TEXT_AREA)
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.ADD, Visible.Mode.VIEW})
    @Tab(label = "Options")
    private String attributes;

    @Transient
    @Label("Clusters")
    @Position(60)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Formattable(negativeValue = Formattable.NA)
    @Description("The number of clusters part of an environment")
    private int clusterCount;

    @Transient
    @Label("Servers")
    @Position(61)
    @Visible(modes = {Visible.Mode.BROWSE})
    @Formattable(negativeValue = Formattable.NA)
    @Description("The number of servers part of an environment")
    private int serverCount;

    @Column(name = "version",length = 50)
    @Description("The version of the environment")
    @Position(100)
    @Filterable
    @Tab(label = "Options")
    private String version;

    @Visible(value = false)
    @ManyToMany()
    @JoinTable(name="infrastructure_environment_to_cluster", joinColumns = @JoinColumn(name = "environment_id"), inverseJoinColumns = @JoinColumn(name = "cluster_id"))
    private Collection<Cluster> clusters;

    @Visible(value = false)
    @ManyToMany()
    @JoinTable(name="infrastructure_environment_to_server", joinColumns = @JoinColumn(name = "environment_id"), inverseJoinColumns = @JoinColumn(name = "server_id"))
    private Collection<Server> servers;
}
