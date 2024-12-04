package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSetNotFoundException;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.api.RestService;
import net.microfalx.heimdall.rest.api.Simulator;
import net.microfalx.heimdall.rest.core.common.SimulationControllerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller("OverviewSimulationRunningController")
@RequestMapping("/rest/simulation/running")
@DataSet(model = SimulationRunning.class, timeFilter = false)
public class SimulationRunningController extends DataSetController<SimulationRunning, String> {

    @Autowired
    protected RestService restService;

    @Autowired
    protected ContentService contentService;

    @GetMapping("/log/{id}")
    public String viewLog(@PathVariable("id") String id, Model model) throws IOException {
        return getHelper(id, model).viewLog();
    }

    @GetMapping("/data/{id}")
    public String viewData(@PathVariable("id") String id, Model model) throws IOException {
        return getHelper(id, model).viewData();
    }

    @GetMapping("/report/{id}")
    public String viewReport(@PathVariable("id") String id, Model model) throws IOException {
        return getHelper(id, model).viewReport();
    }

    private SimulationControllerHelper getHelper(String id, Model model) {
        Simulator selectedSimulator = restService.getRunning().stream().filter(simulator -> simulator.getId().equals(id))
                .findFirst().orElseThrow(() -> new DataSetNotFoundException("Simulation is no longer running"));
        return new SimulationControllerHelper(restService, contentService, model).setSimulator(selectedSimulator);
    }
}
