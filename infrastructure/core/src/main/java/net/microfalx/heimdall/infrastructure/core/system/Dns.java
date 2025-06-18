package net.microfalx.heimdall.infrastructure.core.system;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Tabs;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "infrastructure_dns")
@Name("DNS")
@Getter
@Setter
@ToString(callSuper = true)
@Tabs(attributes = {"tags", "description"})
public class Dns extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

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
