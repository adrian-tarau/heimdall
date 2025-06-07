package net.microfalx.heimdall.llm.core.jpa;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository("CoreModelRepository")
public interface ModelRepository extends NaturalJpaRepository<Model,Integer>, JpaSpecificationExecutor<Model> {
}
