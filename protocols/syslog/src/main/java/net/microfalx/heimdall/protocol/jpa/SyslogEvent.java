package net.microfalx.heimdall.protocol.jpa;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Lookup;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.syslog.lookup.FacilityLookup;
import net.microfalx.heimdall.protocol.syslog.lookup.SeverityLookup;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.ReadOnly;

@Entity
@Table(name = "protocol_syslog_events")
@Name("Syslog")
@ReadOnly
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true)
public class SyslogEvent extends Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    @Position(1)
    private Address address;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    @Name
    @Position(1)
    private Part message;

    @Column(name = "severity", nullable = false)
    @Lookup(model = SeverityLookup.class)
    @Position(10)
    private Integer severity;

    @Column(name = "facility", nullable = false)
    @Lookup(model = FacilityLookup.class)
    @Position(10)
    private Integer facility;
}
