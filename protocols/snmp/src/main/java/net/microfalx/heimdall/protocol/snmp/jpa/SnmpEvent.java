package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.snmp.lookup.TrapLookup;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "protocol_snmp_events")
@Name("SNMP")
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
    @Description("The name of the host, source or application that sent the SNMP event")
    private Address agentAddress;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    @Name
    @Position(10)
    @Description("The content of the SNMP event")
    private Part message;

    @ManyToOne
    @JoinColumn(name = "bindings_id", nullable = false)
    @NotNull
    @Position(10)
    @Label("Bindings")
    @Visible(value = false)
    @Filterable
    private Part bindingPart;

    @Column(name = "version")
    @Visible(modes = Visible.Mode.VIEW)
    @Position(20)
    private int version;

    @Column(name = "community_string", length = 200)
    @NotBlank
    @Visible(modes = Visible.Mode.VIEW)
    @Position(21)
    @Filterable
    private String communityString;

    @Column(name = "enterprise", length = 200)
    @Description("The unique identifier of the SNMP agent that is sending the trap")
    private String enterprise;

    @Column(name = "trap_type")
    @Lookup(model = TrapLookup.class)
    @Description("The type of trap for the SNMP event")
    private Integer trapType;
}
