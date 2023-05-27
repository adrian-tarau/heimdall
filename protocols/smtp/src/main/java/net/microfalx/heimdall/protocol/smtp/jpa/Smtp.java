package net.microfalx.heimdall.protocol.smtp.jpa;

import jakarta.persistence.*;
import net.microfalx.heimdall.protocol.core.TimestampAware;
import net.microfalx.heimdall.protocol.core.jpa.Address;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Entity
@Table(name = "smtps")
public class Smtp extends TimestampAware {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject", length = 500)
    private String subject;

    @ManyToOne
    @JoinColumn(name = "from_id", nullable = false)
    private Address from;

    @ManyToOne
    @JoinColumn(name = "to_id", nullable = false)
    private Address to;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "smtp_id")
    private Collection<SmtpAttachment> attachments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        requireNonNull(subject);
        this.subject = subject;
    }

    public Address getFrom() {
        return from;
    }

    public void setFrom(Address from) {
        requireNonNull(from);
        this.from = from;
    }

    public Address getTo() {
        return to;
    }

    public void setTo(Address to) {
        requireNonNull(to);
        this.to = to;
    }

    public Collection<SmtpAttachment> getAttachments() {
        return attachments;
    }

    public void addAttachment(SmtpAttachment attachment) {
        requireNonNull(attachment);
        this.attachments.add(attachment);
    }

    public void removeAttachment(SmtpAttachment attachment) {
        requireNonNull(attachment);
        this.attachments.remove(attachment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Smtp smtp)) return false;

        return Objects.equals(id, smtp.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Smtp{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", from=" + from +
                ", to=" + to +
                "} " + super.toString();
    }
}
