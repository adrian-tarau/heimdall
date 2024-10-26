package net.microfalx.heimdall.rest.core.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RestProjectRepository extends JpaRepository<RestProject,Integer>, JpaSpecificationExecutor<RestProject> {
}
