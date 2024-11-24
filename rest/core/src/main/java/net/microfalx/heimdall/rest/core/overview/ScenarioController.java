package net.microfalx.heimdall.rest.core.overview;

import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("OverviewScenarioController")
@DataSet(model = Scenario.class)
@RequestMapping("/rest/scenario")
public class ScenarioController extends DataSetController<Scenario,Integer> {
}
