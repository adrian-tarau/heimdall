package net.microfalx.heimdall.llm.web.system.jpa;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository("WebModelRepository")
public interface ModelRepository extends NaturalJpaRepository<Model,Integer>, JpaSpecificationExecutor<Model> {
}
