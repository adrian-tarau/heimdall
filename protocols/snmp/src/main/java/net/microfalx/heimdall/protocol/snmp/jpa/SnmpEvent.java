package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.snmp.lookup.TrapLookup;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "protocol_snmp_events")
@Name("MIBs")
@ReadOnly
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class SnmpEvent extends Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agent_address_id", nullable = false)
    @NotNull
    @Position(1)
    @Label("Address")
    private Address agentAddress;

    @ManyToOne
    @JoinColumn(name = "bindings_id", nullable = false)
    @NotNull
    @Name
    @Position(10)
    @Label("Bindings")
    private Part bindingPart;

    @Column(name = "version", nullable = false, length = 50)
    @NotBlank
    @Visible(modes = Visible.Mode.VIEW)
    @Position(20)
    private String version;

    @Column(name = "community_string", nullable = false, length = 200)
    @NotBlank
    @Visible(modes = Visible.Mode.VIEW)
    @Position(21)
    private String communityString;

    @Column(name = "enterprise", nullable = false, length = 200)
    private String enterprise;

    @Column(name = "trap_type")
    @Lookup(model = TrapLookup.class)
    private Integer trapType;
}
