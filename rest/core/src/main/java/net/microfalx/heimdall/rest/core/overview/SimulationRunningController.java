package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewSimulationRunningController")
@RequestMapping("/rest/simulation/running")
@DataSet(model = SimulationRunning.class, timeFilter = false)
public class SimulationRunningController extends DataSetController<SimulationRunning, String> {
}
