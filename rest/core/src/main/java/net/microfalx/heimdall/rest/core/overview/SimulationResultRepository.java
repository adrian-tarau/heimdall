package net.microfalx.heimdall.rest.core.overview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulationResultRepository extends JpaRepository<SimulationResult,Integer>,JpaSpecificationExecutor<SimulationResult> {
}
