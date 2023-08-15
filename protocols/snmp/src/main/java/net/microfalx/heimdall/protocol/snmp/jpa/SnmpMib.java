package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.heimdall.protocol.snmp.mib.MibType;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

@Entity
@Table(name = "protocol_snmp_mibs")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class SnmpMib extends NamedTimestampAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "type", nullable = false)
    @Position(5)
    @Enumerated(EnumType.STRING)
    private MibType type;

    @Column(name = "content", nullable = false)
    @NotBlank
    @Position(10)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.ADD, Visible.Mode.ADD})
    private String content;

}
