package net.microfalx.heimdall.infrastructure.ping;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface PingResultRepository extends JpaRepository<PingResult,Integer>, JpaSpecificationExecutor<PingResult> {
}
