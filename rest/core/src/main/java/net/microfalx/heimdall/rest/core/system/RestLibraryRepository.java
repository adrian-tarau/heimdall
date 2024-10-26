package net.microfalx.heimdall.rest.core.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RestLibraryRepository extends JpaRepository<RestLibrary,Integer>,JpaSpecificationExecutor<RestLibrary>{
}
