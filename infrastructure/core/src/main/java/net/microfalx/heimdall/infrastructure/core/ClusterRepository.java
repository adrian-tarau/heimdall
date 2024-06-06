package net.microfalx.heimdall.infrastructure.core;

import jakarta.transaction.Transactional;
import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface ClusterRepository extends NaturalJpaRepository<Cluster, Integer>, JpaSpecificationExecutor<Cluster>  {
}
