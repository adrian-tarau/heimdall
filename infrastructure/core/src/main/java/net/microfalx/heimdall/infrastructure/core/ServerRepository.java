package net.microfalx.heimdall.infrastructure.core;

import jakarta.transaction.Transactional;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface ServerRepository extends NaturalJpaRepository<Server, Integer>, JpaSpecificationExecutor<Server> {
}
