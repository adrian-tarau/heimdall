package net.microfalx.heimdall.rest.core.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestLibraryHistoryRepository extends JpaRepository<RestLibraryHistory, Integer>, JpaSpecificationExecutor<RestLibraryHistory> {

    /**
     * Find the history for the library
     *
     * @param restLibrary the history of the library
     * @return the history for the library
     */
    List<RestLibraryHistory> findAllByRestLibrary(RestLibrary restLibrary);
}
