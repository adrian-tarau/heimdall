package net.microfalx.heimdall.rest.core.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RestLibraryHistoryRepository extends JpaRepository<RestLibraryHistory,Integer>, JpaSpecificationExecutor<RestLibraryHistory> {
}
