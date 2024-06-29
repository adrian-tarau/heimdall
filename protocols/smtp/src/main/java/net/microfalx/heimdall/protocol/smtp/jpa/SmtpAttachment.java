package net.microfalx.heimdall.protocol.smtp.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.jdbc.entity.IdentityAware;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.lang.annotation.Description;
import net.microfalx.lang.annotation.Position;

@Entity
@Table(name = "protocol_smtp_attachments")
@Getter
@Setter
@ToString
public class SmtpAttachment extends IdentityAware<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "smtp_event_id", nullable = false)
    @Position(100)
    @Description("The attachment that the {name} belongs to")
    private SmtpEvent smtpEvent;

    @OneToOne
    @JoinColumn(name = "part_id", nullable = false)
    @Position(1)
    @Description("The content of SMTP attachment in the {name}")
    private Part part;

}
