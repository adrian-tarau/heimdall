package net.microfalx.heimdall.protocol.gelf.jpa;

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
import net.microfalx.heimdall.protocol.gelf.lookup.FacilityLookup;
import net.microfalx.heimdall.protocol.gelf.lookup.SeverityLookup;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "protocol_gelf_events")
@Name("GELF")
@ReadOnly
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class GelfEvent extends Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    @Visible(false)
    private Long id;

    @JoinColumn(name = "address_id")
    @ManyToOne
    @NotNull
    @Position(1)
    private Address address;

    @JoinColumn(name = "short_message_id")
    @OneToOne
    @Name
    @Position(5)
    @Label("Message")
    private Part shortMessage;

    @JoinColumn(name = "long_message_id")
    @OneToOne
    @Position(6)
    @Visible(modes = Visible.Mode.VIEW)
    private Part longMessage;

    @JoinColumn(name = "fields_id")
    @OneToOne
    @Position(10)
    @Visible(modes = Visible.Mode.VIEW)
    private Part fields;

    @Column(name = "version", length = 50, nullable = false)
    @NotBlank
    @Position(40)
    @Visible(modes = Visible.Mode.VIEW)
    private String version;

    @Column(name = "level", nullable = false)
    @Position(25)
    @Lookup(model = SeverityLookup.class)
    private int level;

    @Column(name = "facility", nullable = false)
    @Position(30)
    @Lookup(model = FacilityLookup.class)
    private int facility;

}