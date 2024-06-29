package net.microfalx.heimdall.infrastructure.core.system;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends NaturalJpaRepository<Server, Integer>, JpaSpecificationExecutor<Server> {
}
