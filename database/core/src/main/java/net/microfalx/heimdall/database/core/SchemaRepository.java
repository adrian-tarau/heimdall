package net.microfalx.heimdall.database.core;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface SchemaRepository extends JpaRepository<Schema, Integer>, JpaSpecificationExecutor<Schema> {
}
