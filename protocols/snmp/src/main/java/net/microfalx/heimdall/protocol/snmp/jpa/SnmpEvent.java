package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.persistence.*;
import net.microfalx.heimdall.protocol.core.jpa.Event;

@Entity
@Table(name = "protocol_snmp_events")
public class SnmpEvent extends Event {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
