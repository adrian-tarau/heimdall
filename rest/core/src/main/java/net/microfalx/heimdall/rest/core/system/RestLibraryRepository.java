package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.jdbc.jpa.NaturalJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RestLibraryRepository extends NaturalJpaRepository<RestLibrary, Integer>, JpaSpecificationExecutor<RestLibrary> {
}