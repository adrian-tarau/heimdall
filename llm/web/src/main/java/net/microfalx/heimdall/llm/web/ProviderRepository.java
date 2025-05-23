package net.microfalx.heimdall.llm.web;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository("WebProviderRepository")
public interface ProviderRepository extends NaturalJpaRepository<Provider,Integer>, JpaSpecificationExecutor<Provider> {
}
