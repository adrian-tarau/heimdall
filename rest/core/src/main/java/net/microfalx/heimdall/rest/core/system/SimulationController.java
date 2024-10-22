package net.microfalx.heimdall.rest.core.system;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("SystemSimulationController")
@DataSet(model = Simulation.class, timeFilter = false)
@RequestMapping("/system/rest/simulation")
public class SimulationController extends DataSetController<Simulation,Integer> {
}
