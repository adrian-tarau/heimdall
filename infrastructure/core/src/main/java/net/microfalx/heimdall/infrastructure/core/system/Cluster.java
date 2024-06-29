package net.microfalx.heimdall.infrastructure.core.system;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.heimdall.infrastructure.api.Server;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "infrastructure_cluster")
@Name("Clusters")
@Getter
@Setter
@ToString(callSuper = true)
public class Cluster extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Column(name = "natural_id", nullable = false)
    @Visible(value = false)
    private String naturalId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Position(10)
    @Description("The type of servers deployed in the cluster")
    private Server.Type type;

    @Column(name = "time_zone", nullable = false)
    @Position(20)
    @Description("The timezone where the cluster is deployed")
    @Lookup(model = TimeZoneLookup.class)
    private String timeZone;
}
