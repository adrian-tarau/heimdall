package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewSimulationResultController")
@DataSet(model = SimulationResult.class, timeFilter = false)
@RequestMapping("/rest/simulation/result")
public class SimulationResultController extends DataSetController<SimulationResult,Long> {
}
