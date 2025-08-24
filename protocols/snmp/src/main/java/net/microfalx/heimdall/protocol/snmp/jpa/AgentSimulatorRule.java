package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.jdbc.entity.surrogate.NamedAndTaggedAndTimestampedIdentityAware;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "protocol_snmp_agent_simulator_rule")
@Getter
@Setter
public class AgentSimulatorRule extends NamedAndTaggedAndTimestampedIdentityAware<Integer> {

    @NaturalId
    @Visible(value = false)
    @Column(name = "natural_id", nullable = false, length = 100, unique = true)
    private String naturalId;

    @Lob
    @Column(name = "content", nullable = false)
    @Position(100)
    @Description("A text file containing the SNMP agent simulator rule")
    @Visible(false)
    private String content;

    @Column(name = "enabled",nullable = false)
    @Position(200)
    @Description("Indicates whether the simulator rule is enabled or not")
    @Width("90px")
    @Filterable()
    private boolean enabled=true;
}
