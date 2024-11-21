package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RestScenarioRepository extends NaturalJpaRepository<RestScenario,Integer>, JpaSpecificationExecutor<RestScenario> {
}
