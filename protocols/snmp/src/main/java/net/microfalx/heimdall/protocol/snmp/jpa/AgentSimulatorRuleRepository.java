package net.microfalx.heimdall.protocol.snmp.jpa;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentSimulatorRuleRepository extends NaturalJpaRepository<AgentSimulatorRule, Integer>, JpaSpecificationExecutor<AgentSimulatorRule> {
}
