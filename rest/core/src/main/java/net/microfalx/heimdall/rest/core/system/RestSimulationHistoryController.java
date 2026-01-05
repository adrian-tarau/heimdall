package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.core.common.AbstractLibraryHistoryController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemRestSimulationHistoryController")
@DataSet(model = RestSimulationHistory.class, timeFilter = false, canAdd = false)
@RequestMapping("/system/rest/simulation/history")
public class RestSimulationHistoryController extends AbstractLibraryHistoryController<RestSimulationHistory, RestSimulation> {

    private final RestSimulationRepository restSimulationRepository;
    private final RestSimulationHistoryRepository restSimulationHistoryRepository;

    public RestSimulationHistoryController(DataSetService dataSetService, RestService restService, ContentService contentService, RestSimulationRepository restSimulationRepository, RestSimulationHistoryRepository restSimulationHistoryRepository) {
        super(dataSetService, restService, contentService);
        this.restSimulationRepository = restSimulationRepository;
        this.restSimulationHistoryRepository = restSimulationHistoryRepository;
    }

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
