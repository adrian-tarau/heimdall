package net.microfalx.heimdall.protocol.syslog.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SyslogEventRepository extends JpaRepository<SyslogEvent, Long>, JpaSpecificationExecutor<SyslogEvent> {
}
