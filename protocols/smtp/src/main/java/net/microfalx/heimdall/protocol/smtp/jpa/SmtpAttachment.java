package net.microfalx.heimdall.protocol.smtp.jpa;

import jakarta.persistence.*;
import net.microfalx.heimdall.protocol.core.jpa.Part;
import net.microfalx.heimdall.protocol.smtp.Attachment;

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

    @OneToMany
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
}
