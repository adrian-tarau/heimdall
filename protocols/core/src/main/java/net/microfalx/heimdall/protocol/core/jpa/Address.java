package net.microfalx.heimdall.protocol.core.jpa;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;

@Entity
@Table(name = "protocol_addresses")
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Address extends NamedTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "type", nullable = false)
    @Position(10)
    @Enumerated(EnumType.STRING)
    @Description("The type of the protocol address")
    private net.microfalx.heimdall.protocol.core.Address.Type type;

    @Column(name = "value")
    @Position(15)
    @Description("The address raw value (IP, email, MAC)")
    private String value;

}
