package net.microfalx.heimdall.protocol.snmp.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Searchable;
import net.microfalx.bootstrap.jdbc.entity.NamedAndTimestampedIdentityAware;
import net.microfalx.heimdall.protocol.snmp.mib.MibType;
import net.microfalx.lang.annotation.*;

@Entity
@Table(name = "protocol_snmp_mibs")
@Getter
@Setter
@ToString
@Name("MIBs")
public class SnmpMib extends NamedAndTimestampedIdentityAware<Long> {

    @Column(name = "type", nullable = false)
    @Position(5)
    @Enumerated(EnumType.STRING)
    @Width(min = "50")
    @Description("A MIB can be either an internal Mib provided by the system. Or an external MIB provided by the user")
    @Visible(modes = Visible.Mode.BROWSE)
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
    @Searchable
    private String enterpriseOid;

    @Column(name = "message_oids")
    @Position(20)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    @Searchable
    private String messageOids;

    @Column(name = "create_at_oids")
    @Position(25)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    @Searchable
    private String createAtOids;

    @Column(name = "sent_at_oids")
    @Position(30)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    @Searchable
    private String sentAtOids;

    @Column(name = "severity_oids")
    @Position(35)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.EDIT})
    @Searchable
    private String severityOids;

    @Column(name = "content")
    @Position(50)
    @Visible(modes = {Visible.Mode.VIEW, Visible.Mode.ADD, Visible.Mode.ADD})
    private String content;

    @Column(name = "file_name", nullable = false)
    @NotBlank
    @Position(6)
    @Description("The file that contains the MIB")
    @Visible(modes = Visible.Mode.BROWSE)
    private String fileName;
}
