package net.microfalx.heimdall.protocol.smtp.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmtpAttachmentRepository extends JpaRepository<SmtpAttachment, Long> {
}
