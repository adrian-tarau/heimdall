package net.microfalx.heimdall.protocol.jpa;

import jakarta.persistence.*;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.lang.annotation.ReadOnly;

import java.util.Objects;

@Entity
@Table(name = "protocol_syslog_events")
@ReadOnly
public class SyslogEvent extends Event {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Part message;

    @Column(name = "severity", nullable = false)
    private Integer severity;

    @Column(name = "facility", nullable = false)
    private Integer facility;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Part getMessage() {
        return message;
    }

    public void setMessage(Part message) {
        this.message = message;
    }

    public Integer getFacility() {
        return facility;
    }

    public void setFacility(Integer facility) {
        this.facility = facility;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyslogEvent that)) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
