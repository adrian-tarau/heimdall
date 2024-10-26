package net.microfalx.heimdall.rest.core.overview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SimulationResultRepository extends JpaRepository<SimulationResult,Integer>,JpaSpecificationExecutor<SimulationResult> {
}
