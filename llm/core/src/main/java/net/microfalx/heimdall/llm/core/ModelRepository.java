package net.microfalx.heimdall.llm.core;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends NaturalJpaRepository<Model,Integer> {
}
