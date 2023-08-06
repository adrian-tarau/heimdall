package net.microfalx.heimdall.protocol.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;

import java.util.Objects;

@Entity
@Table(name = "protocol_gelf_events")
@ReadOnly
public class GelfEvent extends Event {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "address_id")
    @ManyToOne
    @NotNull
    private Address address;

    @JoinColumn(name = "short_message_id")
    @OneToOne
    @Name
    private Part shortMessage;

    @JoinColumn(name = "long_message_id")
    @OneToOne
    private Part longMessage;

    @JoinColumn(name = "fields_id")
    @OneToOne
    private Part fields;

    @Column(name = "version", length = 50, nullable = false)
    @NotBlank
    private String version;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "facility", nullable = false)
    private int facility;

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

    public Part getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(Part shortMessage) {
        this.shortMessage = shortMessage;
    }

    public Part getLongMessage() {
        return longMessage;
    }

    public void setLongMessage(Part longMessage) {
        this.longMessage = longMessage;
    }

    public Part getFields() {
        return fields;
    }

    public void setFields(Part fields) {
        this.fields = fields;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFacility() {
        return facility;
    }

    public void setFacility(int facility) {
        this.facility = facility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GelfEvent gelfEvent)) return false;

        return Objects.equals(id, gelfEvent.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
