package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.heimdall.rest.core.common.AbstractResultController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewSimulationResultController")
@DataSet(model = SimulationResult.class)
@RequestMapping("/rest/simulation/result")
public class SimulationResultController extends AbstractResultController<SimulationResult> {

}
