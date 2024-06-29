package net.microfalx.heimdall.broker.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BrokerSessionRepository extends JpaRepository<BrokerSession, Integer>, JpaSpecificationExecutor<BrokerSession> {
}
