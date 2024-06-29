package net.microfalx.heimdall.database.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatementStatisticsRepository extends JpaRepository<StatementStatistics, Integer> {
}
