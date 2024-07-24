package net.microfalx.heimdall.infrastructure.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Component;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;
import org.hibernate.annotations.NaturalId;

import java.util.Collection;

@Entity
@Table(name = "infrastructure_environment")
@Name("Environments")
@Getter
@Setter
@ToString(callSuper = true)
public class Environment extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "attributes", nullable = false)
    @Position(50)
    @Description("A collection of attributes, one per line, separated by '=' associated with an environment")
    @Component(Component.Type.TEXT_AREA)
    @Visible(modes = {Visible.Mode.EDIT, Visible.Mode.ADD, Visible.Mode.VIEW})
    private String attributes;

    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @ManyToMany()
    @JoinTable(name="infrastructure_environment_to_cluster", joinColumns = @JoinColumn(name = "environment_id"), inverseJoinColumns = @JoinColumn(name = "cluster_id"))
    private Collection<Cluster> clusters;

    @Visible(modes = {Visible.Mode.ADD, Visible.Mode.EDIT})
    @ManyToMany()
    @JoinTable(name="infrastructure_environment_to_server", joinColumns = @JoinColumn(name = "environment_id"), inverseJoinColumns = @JoinColumn(name = "server_id"))
    private Collection<Server> servers;
}
