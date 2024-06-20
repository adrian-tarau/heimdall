package net.microfalx.heimdall.infrastructure.core;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTaggedTimestampAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "infrastructure_dns")
@Name("DNS")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class Dns extends NamedAndTaggedTimestampAware {

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

    @Column(name = "hostname", nullable = false)
    @Position(10)
    @Description("The hostname of the server")
    private String hostname;

    @Column(name = "domain")
    @Position(11)
    @Description("The domain of the server")
    @Width("300px")
    private String domain;

    @Column(name = "ip", nullable = false)
    @Position(12)
    @Description("The IP of the server")
    @Label("IP")
    private String ip;

    @Column(name = "valid", nullable = false)
    @Position(20)
    @Description("Indicates whether the DNS entry is valid (has a valid hostname/domain)")
    @Width("80px")
    @ReadOnly
    private boolean valid;
}
