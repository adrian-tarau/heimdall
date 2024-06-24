package net.microfalx.heimdall.infrastructure.core;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedTimestampAware;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class Cluster extends NamedAndTaggedTimestampAware {

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
    @Position(10)
    @Description("The type of servers deployed in the cluster")
    private Server.Type type;

    @Column(name = "time_zone", nullable = false)
    @Position(20)
    @Description("The timezone where the cluster is deployed")
    @Lookup(model = TimeZoneLookup.class)
    private String timeZone;
}
