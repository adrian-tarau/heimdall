package net.microfalx.heimdall.rest.core.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestSimulationHistoryRepository extends JpaRepository<RestSimulationHistory, Integer>, JpaSpecificationExecutor<RestSimulationHistory> {

    /**
     * Find the history for the simulation
     *
     * @param restSimulation the history of the simulation
     * @return the history for the simulation
     */
    List<RestSimulationHistory> findAllByRestSimulation(RestSimulation restSimulation);
}
