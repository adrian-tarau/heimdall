package net.microfalx.heimdall.database.core;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface StatementRepository extends JpaRepository<Statement, Integer>, JpaSpecificationExecutor<Statement> {

    /**
     * Finds a user by its statement id.
     *
     * @param id the statement identifier
     * @return an optional user
     */
    Optional<Statement> findByStatementId(String id);
}
