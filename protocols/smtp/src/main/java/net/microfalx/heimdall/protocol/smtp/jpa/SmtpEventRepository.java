package net.microfalx.heimdall.protocol.smtp.jpa;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface SmtpEventRepository extends JpaRepository<SmtpEvent, Integer>, JpaSpecificationExecutor {

}
