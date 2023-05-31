package net.microfalx.heimdall.protocol.smtp.jpa;

import jakarta.persistence.*;
import net.microfalx.heimdall.protocol.core.jpa.Part;

import java.util.Objects;

@Entity
@Table(name = "smtp_attachments")
public class SmtpAttachment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "smtp_id", nullable = false)
    private Smtp smtp;

    @OneToOne
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Smtp getSmtp() {
        return smtp;
    }

    public void setSmtp(Smtp smtp) {
        this.smtp = smtp;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmtpAttachment that)) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SmtpAttachment{" +
                "id=" + id +
                ", smtp=" + smtp +
                ", part=" + part +
                '}';
    }
}
