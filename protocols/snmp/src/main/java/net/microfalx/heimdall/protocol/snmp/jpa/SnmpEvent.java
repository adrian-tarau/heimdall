package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;

import java.util.Objects;

@Entity
@Table(name = "protocol_snmp_events")
public class SnmpEvent extends Event {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @JoinColumn(name = "agent_address_id", nullable = false)
    @NotNull
    private Address agentAddress;

    @Column
    @JoinColumn(name = "bindings_id", nullable = false)
    @NotNull
    private Part bindingPart;

    @Column(name = "version", nullable = false, length = 50)
    @NotBlank
    private String version;

    @Column(name = "community_string", nullable = false, length = 200)
    @NotBlank
    private String communityString;

    @Column(name = "enterprise", nullable = false, length = 200)
    private String enterprise;

    @Column(name = "trap_type")
    private Integer trapType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Address getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(Address agentAddress) {
        this.agentAddress = agentAddress;
    }

    public Part getBindingPart() {
        return bindingPart;
    }

    public void setBindingPart(Part bindingPart) {
        this.bindingPart = bindingPart;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommunityString() {
        return communityString;
    }

    public void setCommunityString(String communityString) {
        this.communityString = communityString;
    }

    public String getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public Integer getTrapType() {
        return trapType;
    }

    public void setTrapType(Integer trapType) {
        this.trapType = trapType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SnmpEvent snmpEvent)) return false;

        return Objects.equals(id, snmpEvent.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
