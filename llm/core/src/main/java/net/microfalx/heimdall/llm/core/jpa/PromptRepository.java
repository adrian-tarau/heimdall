package net.microfalx.heimdall.llm.core.jpa;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository("CorePromptRepository")
public interface PromptRepository extends NaturalJpaRepository<Prompt, Integer>, JpaSpecificationExecutor<Prompt> {
}
