package net.microfalx.heimdall.protocol.smtp.jpa;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.heimdall.protocol.core.jpa.Address;
import net.microfalx.heimdall.protocol.core.jpa.Event;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(callSuper = true, exclude = "attachments")
public class SmtpEvent extends Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "subject", length = 500)
    @Name
    @Position(1)
    private String subject;

    @ManyToOne
    @JoinColumn(name = "from_id", nullable = false)
    @Position(2)
    private Address from;

    @ManyToOne
    @JoinColumn(name = "to_id", nullable = false)
    @Position(3)
    private Address to;

    @Column(name = "attachment_count", nullable = false)
    @Label(value = "", icon = "fa-solid fa-paperclip")
    @Position(10)
    private int attachmentCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "smtp_event_id")
    @Visible(false)
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
