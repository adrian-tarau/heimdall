package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.heimdall.rest.core.common.AbstractLibraryHistoryController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestSimulationHistoryController")
@DataSet(model = RestSimulationHistory.class, timeFilter = false, canAdd = false)
@RequestMapping("/system/rest/simulation/history")
public class RestSimulationHistoryController extends AbstractLibraryHistoryController<RestSimulationHistory, RestSimulation> {

    @Autowired
    private RestSimulationRepository restSimulationRepository;

    @Autowired
    private RestSimulationHistoryRepository restSimulationHistoryRepository;

    @Override
    protected String getTitle() {
        return "Simulation";
    }

    @Override
    protected RestSimulationHistory getHistory(int id) {
        return restSimulationHistoryRepository.findById(id).orElse(null);
    }

    @Override
    protected RestSimulation getLibrary(RestSimulationHistory history) {
        return history.getRestSimulation();
    }

    @Override
    protected void save(RestSimulation library) {
        restSimulationRepository.save(library);
    }
}
