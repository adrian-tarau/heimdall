package net.microfalx.heimdall.llm.web.system;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository("WebPromptRepository")
public interface PromptRepository extends NaturalJpaRepository<Prompt,Integer>, JpaSpecificationExecutor<Prompt> {
}
