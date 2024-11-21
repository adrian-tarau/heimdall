package net.microfalx.heimdall.rest.core.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RestResultRepository extends JpaRepository<RestResult,Integer>, JpaSpecificationExecutor<RestResult> {
}
