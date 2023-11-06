package net.microfalx.heimdall.protocol.syslog.jpa;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface SyslogEventRepository extends JpaRepository<SyslogEvent, Long>, JpaSpecificationExecutor<SyslogEvent> {
}
