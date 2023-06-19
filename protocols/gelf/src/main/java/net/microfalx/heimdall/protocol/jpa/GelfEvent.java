package net.microfalx.heimdall.protocol.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.microfalx.heimdall.protocol.core.TimestampAware;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Part;

import java.util.Objects;

@Entity
@Table(name = "protocol_gelf_events")
public class GelfEvent extends TimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "address_id")
    @ManyToOne
    @NotNull
    private Address address;

    @JoinColumn(name = "short_message_id")
    @OneToOne
    private Part shortMessage;

    @JoinColumn(name = "long_message_id")
    @OneToOne
    private Part longMessage;

    @Column(name = "version", length = 50, nullable = false)
    @NotBlank
    private String version;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "facility", nullable = false)
    private Integer facility;

    @Column(name = "fields", nullable = false, length = 4000)
    private String fields;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getFacility() {
        return facility;
    }

    public void setFacility(Integer facility) {
        this.facility = facility;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
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
