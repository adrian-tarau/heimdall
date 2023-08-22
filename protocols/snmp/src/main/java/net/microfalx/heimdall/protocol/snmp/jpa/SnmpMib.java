package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.NamedTimestampAware;
import net.microfalx.heimdall.protocol.snmp.mib.MibType;
import net.microfalx.lang.annotation.Label;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Position;
import net.microfalx.lang.annotation.Visible;

@Entity
@Table(name = "protocol_snmp_mibs")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Name("MIBs")
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

    @Column(name = "module_id", nullable = false)
    @NotBlank
    @Position(10)
    @Visible(modes = {Visible.Mode.VIEW})
    private String moduleId;

    @Column(name = "enterprise_oid")
    @Position(15)
    @Label("Enterprise OID")
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    private String enterpriseOid;

    @Column(name = "message_oids")
    @Position(20)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    private String messageOids;

    @Column(name = "create_at_oids")
    @Position(25)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    private String createAtOids;

    @Column(name = "sent_at_oids")
    @Position(30)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    private String sentAtOids;

    @Column(name = "severity_oids")
    @Position(35)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    private String severityOids;

    @Column(name = "content")
    @Position(50)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.ADD, Visible.Mode.ADD})
    private String content;

    @Column(name = "file_name", nullable = false)
    @NotBlank
    @Position(6)
    private String fileName;
}
