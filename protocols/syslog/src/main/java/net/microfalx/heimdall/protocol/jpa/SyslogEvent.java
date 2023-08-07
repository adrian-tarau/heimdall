package net.microfalx.heimdall.protocol.jpa;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.ReadOnly;

@Entity
@Table(name = "protocol_syslog_events")
@ReadOnly
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true,callSuper = false)
@ToString(callSuper = true)
public class SyslogEvent extends Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    @Name
    private Part message;

    @Column(name = "severity", nullable = false)
    private Integer severity;

    @Column(name = "facility", nullable = false)
    private Integer facility;
}
