package net.microfalx.heimdall.protocol.jpa;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface SyslogEventRepository extends JpaRepository<SyslogEvent, Long> {
}
