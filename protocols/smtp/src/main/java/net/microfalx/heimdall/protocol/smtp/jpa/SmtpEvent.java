package net.microfalx.heimdall.protocol.smtp.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.lang.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Entity
@Table(name = "protocol_smtp_events")
@Name("SMTP")
@ReadOnly
@Getter
@Setter
@ToString(callSuper = true, exclude = "attachments")
public class SmtpEvent extends Event {

    @Column(name = "subject", length = 500)
    @Name
    @Position(1)
    @Description("The subject in the email")
    @Width("30%")
    private String subject;

    @ManyToOne
    @JoinColumn(name = "from_id", nullable = false)
    @Position(2)
    @Description("the host that is sending the email")
    private Address from;

    @ManyToOne
    @JoinColumn(name = "to_id", nullable = false)
    @Position(3)
    @Description("the host that is receiving the email")
    private Address to;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    @Visible(value = false)
    private Part message;

    @Column(name = "attachment_count", nullable = false)
    @Label(value = "-", icon = "fa-solid fa-paperclip")
    @Position(10)
    @Description("The number of attachments in the email")
    private int attachmentCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "smtp_event_id")
    @Visible(false)
    @Filterable
    private Collection<SmtpAttachment> attachments = new ArrayList<>();

    public void addAttachment(SmtpAttachment attachment) {
        requireNonNull(attachment);
        this.attachments.add(attachment);
    }

    public void removeAttachment(SmtpAttachment attachment) {
        requireNonNull(attachment);
        this.attachments.remove(attachment);
    }
}
