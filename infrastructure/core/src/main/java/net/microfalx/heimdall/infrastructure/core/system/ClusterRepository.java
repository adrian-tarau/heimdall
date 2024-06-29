package net.microfalx.heimdall.infrastructure.core.system;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterRepository extends NaturalJpaRepository<Cluster, Integer>, JpaSpecificationExecutor<Cluster>  {
}
