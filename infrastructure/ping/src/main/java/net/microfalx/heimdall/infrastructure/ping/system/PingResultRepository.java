package net.microfalx.heimdall.infrastructure.ping.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PingResultRepository extends JpaRepository<PingResult,Integer>, JpaSpecificationExecutor<PingResult> {
}
