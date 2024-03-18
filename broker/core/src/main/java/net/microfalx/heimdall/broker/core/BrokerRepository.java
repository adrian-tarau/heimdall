package net.microfalx.heimdall.broker.core;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface BrokerRepository extends JpaRepository<Broker, Integer>, JpaSpecificationExecutor<Broker> {
}
