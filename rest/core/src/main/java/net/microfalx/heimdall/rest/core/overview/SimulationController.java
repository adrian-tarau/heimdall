package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.heimdall.rest.core.system.RestSimulation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewSimulationController")
@DataSet(model = RestSimulation.class, timeFilter = false)
@RequestMapping("/rest/simulation")
public class SimulationController extends DataSetController<RestSimulation,Integer> {
}
